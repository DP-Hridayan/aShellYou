#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <thread>
#include <algorithm>
#include "llama.h"

#define TAG "AShellLlama"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ─────────────────────────────────────────────────────────────────────────────
// Global state (single model instance at a time)
// ─────────────────────────────────────────────────────────────────────────────
static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
static int g_n_ctx = 2048;

// ─────────────────────────────────────────────────────────────────────────────
// Helper: build a ChatML-formatted prompt
// ─────────────────────────────────────────────────────────────────────────────
static std::string build_chatml_prompt(const std::string &system_prompt,
                                       const std::string &user_prompt) {
    std::string prompt;
    prompt.reserve(system_prompt.size() + user_prompt.size() + 128);
    prompt += "<|im_start|>system\n";
    prompt += system_prompt;
    prompt += "\n<|im_end|>\n";
    prompt += "<|im_start|>user\n";
    prompt += user_prompt;
    prompt += "\n<|im_end|>\n";
    prompt += "<|im_start|>assistant\n";
    return prompt;
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: recreate the context (replaces the removed llama_kv_cache_clear)
// ─────────────────────────────────────────────────────────────────────────────
static bool recreate_context() {
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (!g_model) return false;

    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx = g_n_ctx;
    ctx_params.n_batch = 512;

    // Optimize CPU threads for mobile devices (cap at 4 threads to avoid little core bottlenecks)
    unsigned int hardware_threads = std::thread::hardware_concurrency();
    int num_threads = (hardware_threads > 0) ? std::min(4, (int)hardware_threads) : 4;
    ctx_params.n_threads = num_threads;
    ctx_params.n_threads_batch = num_threads;

    LOGI("Recreating context with %d threads", num_threads);

    g_ctx = llama_init_from_model(g_model, ctx_params);
    return g_ctx != nullptr;
}

// ─────────────────────────────────────────────────────────────────────────────
// JNI: loadModel
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT jboolean JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_loadModel(
        JNIEnv *env, jobject /* this */,
        jstring model_path, jint context_size) {

    // Unload any previously loaded model
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }

    const char *path = env->GetStringUTFChars(model_path, nullptr);
    if (!path) {
        LOGE("Failed to get model path string");
        return JNI_FALSE;
    }

    LOGI("Loading model from: %s (context size: %d)", path, context_size);

    // Initialize llama backend
    llama_backend_init();

    // Load model
    auto model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }

    // Create context
    g_n_ctx = context_size;
    if (!recreate_context()) {
        LOGE("Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    LOGI("Model loaded successfully. Context size: %d", g_n_ctx);
    return JNI_TRUE;
}

// ─────────────────────────────────────────────────────────────────────────────
// JNI: runInference
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT jstring JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_runInference(
        JNIEnv *env, jobject /* this */,
        jstring system_prompt, jstring user_prompt,
        jint max_tokens, jfloat temperature) {

    if (!g_model || !g_ctx) {
        LOGE("Model not loaded");
        return env->NewStringUTF(
                "{\"status\":\"INVALID\",\"description\":\"Model not loaded\",\"dangerLevel\":\"SAFE\"}");
    }

    const char *sys_str = env->GetStringUTFChars(system_prompt, nullptr);
    const char *user_str = env->GetStringUTFChars(user_prompt, nullptr);

    std::string prompt = build_chatml_prompt(
            std::string(sys_str),
            std::string(user_str)
    );

    env->ReleaseStringUTFChars(system_prompt, sys_str);
    env->ReleaseStringUTFChars(user_prompt, user_str);

    // Tokenize the prompt
    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    const int n_prompt_max = prompt.size() + 128;
    std::vector<llama_token> tokens(n_prompt_max);
    const int n_tokens = llama_tokenize(
            vocab,
            prompt.c_str(),
            prompt.size(),
            tokens.data(),
            n_prompt_max,
            true,   // add_special (BOS)
            true    // parse_special
    );

    if (n_tokens < 0) {
        LOGE("Tokenization failed (n_tokens=%d)", n_tokens);
        return env->NewStringUTF(
                "{\"status\":\"INVALID\",\"description\":\"Tokenization failed\",\"dangerLevel\":\"SAFE\"}");
    }
    tokens.resize(n_tokens);

    LOGI("Prompt tokens: %d, max generation tokens: %d", n_tokens, max_tokens);

    // Check that prompt fits in context
    if (n_tokens + max_tokens > g_n_ctx) {
        LOGE("Prompt + generation exceeds context size (%d + %d > %d)",
             n_tokens, max_tokens, g_n_ctx);
        return env->NewStringUTF(
                "{\"status\":\"INVALID\",\"description\":\"Input too long for context window\",\"dangerLevel\":\"SAFE\"}");
    }

    // Recreate context to clear KV cache (replaces removed llama_kv_cache_clear)
    if (!recreate_context()) {
        LOGE("Failed to recreate context");
        return env->NewStringUTF(
                "{\"status\":\"INVALID\",\"description\":\"Context creation failed\",\"dangerLevel\":\"SAFE\"}");
    }

    // Create sampler chain
    auto *smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(42));

    // Process prompt in a single batch
    llama_batch batch = llama_batch_get_one(tokens.data(), n_tokens);
    if (llama_decode(g_ctx, batch) != 0) {
        LOGE("Prompt decode failed");
        llama_sampler_free(smpl);
        return env->NewStringUTF(
                "{\"status\":\"INVALID\",\"description\":\"Inference failed\",\"dangerLevel\":\"SAFE\"}");
    }

    // Generate tokens
    std::string output;
    output.reserve(max_tokens * 8);

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token = llama_sampler_sample(smpl, g_ctx, -1);

        // Stop at EOS / end-of-generation
        if (llama_vocab_is_eog(vocab, new_token)) {
            break;
        }

        // Convert token to text
        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            output.append(buf, n);
        }

        // Check for ChatML end token
        if (output.find("<|im_end|>") != std::string::npos) {
            // Remove the end token from output
            size_t pos = output.find("<|im_end|>");
            output = output.substr(0, pos);
            break;
        }

        // Prepare next batch (single token)
        llama_batch next_batch = llama_batch_get_one(&new_token, 1);
        if (llama_decode(g_ctx, next_batch) != 0) {
            LOGE("Decode failed at token %d", i);
            break;
        }
    }

    llama_sampler_free(smpl);

    LOGI("Generated %zu chars", output.size());
    return env->NewStringUTF(output.c_str());
}

// ─────────────────────────────────────────────────────────────────────────────
// JNI: unloadModel
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT void JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_unloadModel(
        JNIEnv * /* env */, jobject /* this */) {

    LOGI("Unloading model");

    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }

    llama_backend_free();

    LOGI("Model unloaded");
}

// ─────────────────────────────────────────────────────────────────────────────
// JNI: isModelLoaded
// ─────────────────────────────────────────────────────────────────────────────
extern "C" JNIEXPORT jboolean JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_isModelLoaded(
        JNIEnv * /* env */, jobject /* this */) {
    return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}
