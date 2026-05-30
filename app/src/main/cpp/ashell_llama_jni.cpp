#include <jni.h>
#include <string>
#include <vector>
#include <mutex>
#include <thread>
#include <algorithm>
#include <stdexcept>
#include <android/log.h>
#include "llama.h"

#define TAG "AShellLlama"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Global state variables
static std::mutex g_mutex;
static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
static int g_n_ctx = 2048;
static std::vector<llama_token> g_system_tokens;
static bool g_system_prompt_evaluated = false;
static std::string g_cached_system_prompt;

/**
 * RAII wrapper to safely release GetStringUTFChars strings on destruction.
 * Prevents native memory leaks on early return paths.
 */
class JniString {
public:
    JniString(JNIEnv *env, jstring jstr) : m_env(env), m_jstr(jstr) {
        m_str = jstr ? env->GetStringUTFChars(jstr, nullptr) : nullptr;
    }

    ~JniString() {
        if (m_str) {
            m_env->ReleaseStringUTFChars(m_jstr, m_str);
        }
    }

    // Disable copy semantics to guarantee single release
    JniString(const JniString &) = delete;

    JniString &operator=(const JniString &) = delete;

    // Enable move semantics
    JniString(JniString &&other) noexcept: m_env(other.m_env), m_jstr(other.m_jstr),
                                           m_str(other.m_str) {
        other.m_str = nullptr;
    }

    JniString &operator=(JniString &&other) noexcept {
        if (this != &other) {
            if (m_str) {
                m_env->ReleaseStringUTFChars(m_jstr, m_str);
            }
            m_env = other.m_env;
            m_jstr = other.m_jstr;
            m_str = other.m_str;
            other.m_str = nullptr;
        }
        return *this;
    }

    [[nodiscard]] const char *c_str() const { return m_str; }

    [[nodiscard]] bool valid() const { return m_str != nullptr; }

    [[nodiscard]] std::string to_std_string() const { return m_str ? std::string(m_str) : ""; }

private:
    JNIEnv *m_env;
    jstring m_jstr;
    const char *m_str;
};

/**
 * Reusable helper to format a JSON error response string.
 */
static jstring make_json_error(JNIEnv *env, const char *status, const char *description) {
    std::string json = R"({"status":")";
    json += status;
    json += R"(","description":")";
    json += description;
    json += R"(","dangerLevel":"SAFE"})";
    return env->NewStringUTF(json.c_str());
}



/**
 * Recreates the llama context to clear the KV cache.
 * Optimizes the thread count for the specific mobile device.
 */
static bool recreate_context() {
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (!g_model) return false;

    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx = static_cast<uint32_t>(g_n_ctx);
    ctx_params.n_batch = 2048;

    // Use 4 threads to target high-performance cores and avoid little-core bottlenecks
    unsigned int hardware_threads = std::thread::hardware_concurrency();
    int num_threads = (hardware_threads > 0) ? std::min(4, static_cast<int>(hardware_threads)) : 4;
    ctx_params.n_threads = num_threads;
    ctx_params.n_threads_batch = num_threads;

    LOGI("Recreating context with %d threads", num_threads);

    g_ctx = llama_init_from_model(g_model, ctx_params);
    return g_ctx != nullptr;
}

/**
 * JNI method to load a GGUF model and initialize the context.
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_loadModel(
        JNIEnv *env, jobject /* this */,
        jstring model_path, jint context_size) {

    std::lock_guard<std::mutex> lock(g_mutex);

    if (context_size <= 0) {
        LOGE("Invalid context size: %d", context_size);
        return JNI_FALSE;
    }

    // Free any currently loaded model and context
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }

    JniString path(env, model_path);
    if (!path.valid()) {
        LOGE("Failed to get model path string");
        return JNI_FALSE;
    }

    LOGI("Loading model from: %s (context size: %d)", path.c_str(), context_size);

    llama_backend_init();

    auto model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path.c_str(), model_params);

    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }

    g_n_ctx = static_cast<int>(context_size);
    g_system_tokens.clear();
    g_system_prompt_evaluated = false;
    g_cached_system_prompt.clear();
    if (!recreate_context()) {
        LOGE("Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    LOGI("Model loaded successfully. Context size: %d", g_n_ctx);
    return JNI_TRUE;
}

/**
 * Internal inference implementation.
 * Separated from the JNI entry point so the caller can wrap it in try-catch.
 * Implements chunked batch decoding to respect n_batch limits.
 */
/**
 * Internal inference implementation.
 * Separated from the JNI entry point so the caller can wrap it in try-catch.
 * Implements KV Cache prompt caching using standard llama_batch_get_one.
 */
static jstring run_inference_internal(JNIEnv *env,
                                      const JniString &sys_str,
                                      const JniString &user_str,
                                      jint max_tokens,
                                      jfloat temperature) {

    const llama_vocab *vocab = llama_model_get_vocab(g_model);
    if (!vocab) {
        LOGE("Failed to retrieve model vocabulary");
        return make_json_error(env, "INVALID", "Failed to retrieve model vocabulary");
    }

    // Build the system-part and user-part ChatML prompts
    std::string sys_prompt = "<|im_start|>system\n" + sys_str.to_std_string() + "\n<|im_end|>\n";
    std::string user_prompt = "<|im_start|>user\n" + user_str.to_std_string() + "\n<|im_end|>\n<|im_start|>assistant\n";
    std::string combined_prompt = sys_prompt + user_prompt;

    llama_memory_t mem = llama_get_memory(g_ctx);

    // 1. Tokenize system prompt to determine the boundary N
    // Invalidate cache if the system prompt text has changed
    if (sys_prompt != g_cached_system_prompt) {
        if (!g_cached_system_prompt.empty()) {
            LOGI("System prompt changed, invalidating cache");
        }
        g_system_tokens.clear();
        g_system_prompt_evaluated = false;
        g_cached_system_prompt = sys_prompt;
    }
    if (g_system_tokens.empty()) {
        const size_t sys_max_tokens = sys_prompt.size() + 128;
        std::vector<llama_token> sys_temp_tokens(sys_max_tokens);
        int sys_n_tokens = llama_tokenize(
                vocab,
                sys_prompt.c_str(),
                static_cast<int32_t>(sys_prompt.size()),
                sys_temp_tokens.data(),
                static_cast<int32_t>(sys_max_tokens),
                true, // Add special (BOS)
                true  // parse_special
        );
        if (sys_n_tokens < 0) {
            LOGE("Failed to tokenize system prompt (sys_n_tokens=%d)", sys_n_tokens);
            return make_json_error(env, "INVALID", "System prompt tokenization failed");
        }
        g_system_tokens.resize(static_cast<size_t>(sys_n_tokens));
        std::copy(sys_temp_tokens.begin(), sys_temp_tokens.begin() + sys_n_tokens, g_system_tokens.begin());
    }

    const size_t N = g_system_tokens.size();

    // 2. Tokenize the combined prompt (exactly as in the working version)
    const size_t combined_max_tokens = combined_prompt.size() + 128;
    std::vector<llama_token> combined_tokens(combined_max_tokens);
    int combined_n_tokens = llama_tokenize(
            vocab,
            combined_prompt.c_str(),
            static_cast<int32_t>(combined_prompt.size()),
            combined_tokens.data(),
            static_cast<int32_t>(combined_max_tokens),
            true, // Add special (BOS)
            true  // parse_special
    );

    if (combined_n_tokens < 0) {
        LOGE("Failed to tokenize combined prompt (combined_n_tokens=%d)", combined_n_tokens);
        return make_json_error(env, "INVALID", "Combined prompt tokenization failed");
    }
    combined_tokens.resize(static_cast<size_t>(combined_n_tokens));

    // Verify that combined prompt tokens start with system prompt tokens
    if (combined_tokens.size() < N) {
        LOGE("Combined tokens size is smaller than system prompt tokens (%zu < %zu)", combined_tokens.size(), N);
        return make_json_error(env, "INVALID", "Tokenization alignment error");
    }

    // Extract user tokens
    std::vector<llama_token> user_tokens(combined_tokens.begin() + N, combined_tokens.end());
    int user_n_tokens = static_cast<int>(user_tokens.size());

    LOGI("System tokens: %zu, User tokens: %d, max generation tokens: %d", N, user_n_tokens, max_tokens);

    const int total_context_tokens = static_cast<int>(N) + user_n_tokens + max_tokens;
    if (total_context_tokens > g_n_ctx) {
        LOGE("Total tokens exceed context size (%d > %d)", total_context_tokens, g_n_ctx);
        return make_json_error(env, "INVALID", "Input too long for context window");
    }

    // 3. Handle system prompt evaluation (caching)
    if (!g_system_prompt_evaluated) {
        LOGI("System prompt not cached. Evaluating now...");
        
        // WIPE KV Cache completely using llama_memory_seq_rm
        llama_memory_seq_rm(mem, -1, 0, -1);

        // Evaluate system prompt in one batch (n_batch=2048 handles full prompt)
        const int n_batch = 2048;
        for (int i = 0; i < static_cast<int>(N); i += n_batch) {
            int chunk_size = std::min(n_batch, static_cast<int>(N) - i);
            llama_batch batch = llama_batch_get_one(g_system_tokens.data() + i, chunk_size);
            int32_t decode_res = llama_decode(g_ctx, batch);
            if (decode_res != 0) {
                LOGE("Failed to decode system prompt at chunk %d (res=%d)", i, decode_res);
                g_system_tokens.clear();
                g_system_prompt_evaluated = false;
                return make_json_error(env, "INVALID", "System prompt decode failed");
            }
        }

        g_system_prompt_evaluated = true;
        LOGI("System prompt cached successfully (%zu tokens)", N);
    } else {
        LOGI("System prompt already cached (%zu tokens). Reusing cache...", N);
        
        // Remove only previous user/assistant tokens from KV Cache, keeping system prompt intact!
        llama_memory_seq_rm(mem, -1, static_cast<llama_pos>(N), -1);
    }

    // Use greedy sampler for deterministic, fastest-possible token selection
    auto *smpl = llama_sampler_init_greedy();
    if (!smpl) {
        LOGE("Failed to initialize greedy sampler");
        return make_json_error(env, "INVALID", "Sampler initialization failed");
    }

    // Decode the user prompt in one batch (n_batch=2048 handles full prompt)
    const int n_batch = 2048;
    for (int i = 0; i < user_n_tokens; i += n_batch) {
        int chunk_size = std::min(n_batch, user_n_tokens - i);
        llama_batch batch = llama_batch_get_one(user_tokens.data() + i, chunk_size);
        int32_t decode_res = llama_decode(g_ctx, batch);
        if (decode_res != 0) {
            LOGE("Failed to decode user prompt chunk at offset %d (res=%d)", i, decode_res);
            llama_sampler_free(smpl);
            return make_json_error(env, "INVALID", "User prompt decode failed");
        }
    }

    // 4. Generate response tokens
    std::string output;
    output.reserve(static_cast<size_t>(max_tokens) * 8);

    for (int i = 0; i < max_tokens; i++) {
        llama_token new_token = llama_sampler_sample(smpl, g_ctx, -1);

        if (llama_vocab_is_eog(vocab, new_token)) {
            break;
        }

        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            output.append(buf, static_cast<size_t>(n));
        }

        // Stop early if the model tries to generate the next few-shot example or a new line
        if (output.find("\n") != std::string::npos ||
            output.find("Command:") != std::string::npos ||
            output.find("Description:") != std::string::npos) {
            size_t pos = output.find("\n");
            if (pos != std::string::npos) {
                output = output.substr(0, pos);
            }
            break;
        }

        // Check for ChatML closing tag
        if (output.size() >= 10 &&
            output.rfind("<|im_end|>", output.size() - 10) != std::string::npos) {
            size_t pos = output.rfind("<|im_end|>");
            output = output.substr(0, pos);
            break;
        }

        // Decode the generated token
        llama_batch next_batch = llama_batch_get_one(&new_token, 1);
        int32_t decode_res = llama_decode(g_ctx, next_batch);
        if (decode_res != 0) {
            LOGE("Decode failed at generated token %d (res=%d)", i, decode_res);
            break;
        }
    }

    llama_sampler_free(smpl);

    LOGI("Generated %zu chars", output.size());
    return env->NewStringUTF(output.c_str());
}

/**
 * JNI method to execute local llama.cpp model inference.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_runInference(
        JNIEnv *env, jobject /* this */,
        jstring system_prompt, jstring user_prompt,
        jint max_tokens, jfloat temperature) {

    std::lock_guard<std::mutex> lock(g_mutex);

    if (!g_model || !g_ctx) {
        LOGE("Model not loaded");
        return make_json_error(env, "INVALID", "Model not loaded");
    }

    if (max_tokens <= 0) {
        LOGE("Invalid max_tokens: %d", max_tokens);
        return make_json_error(env, "INVALID", "Invalid max_tokens value");
    }

    JniString sys_str(env, system_prompt);
    JniString user_str(env, user_prompt);

    if (!sys_str.valid() || !user_str.valid()) {
        LOGE("Failed to extract prompt strings");
        return make_json_error(env, "INVALID", "Failed to extract prompt strings");
    }

    // Wrap the entire inference pipeline in try-catch to prevent native crashes
    // from killing the app process (e.g., OOM in llama_decode, bad alloc, etc.)
    try {
        return run_inference_internal(env, sys_str, user_str, max_tokens, temperature);
    } catch (const std::bad_alloc &e) {
        LOGE("Out of memory during inference: %s", e.what());
        return make_json_error(env, "INVALID", "Out of memory during inference");
    } catch (const std::exception &e) {
        LOGE("Native exception during inference: %s", e.what());
        return make_json_error(env, "INVALID", "Native inference error");
    } catch (...) {
        LOGE("Unknown native exception during inference");
        return make_json_error(env, "INVALID", "Unknown native error during inference");
    }
}

/**
 * JNI method to unload the active model and free native context resources.
 */
extern "C" JNIEXPORT void JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_unloadModel(
        JNIEnv * /* env */, jobject /* this */) {

    std::lock_guard<std::mutex> lock(g_mutex);
    LOGI("Unloading model");

    g_system_tokens.clear();
    g_system_prompt_evaluated = false;
    g_cached_system_prompt.clear();

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

/**
 * JNI method to check if a model is currently loaded.
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_in_hridayan_ashell_ai_native_LlamaCppBridge_isModelLoaded(
        JNIEnv * /* env */, jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}
