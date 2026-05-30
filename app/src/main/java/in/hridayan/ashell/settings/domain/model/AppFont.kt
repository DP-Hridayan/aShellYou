package `in`.hridayan.ashell.settings.domain.model

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import `in`.hridayan.ashell.R

enum class AppFont(val id: Int, val labelResId: Int) {
    SYSTEM(0, R.string.system_font) {
        override val fontFamily: FontFamily = FontFamily.Default
    },
    MONOSPACE(1, R.string.monospace) {
        override val fontFamily: FontFamily = FontFamily.Monospace
    },
    SANS_SERIF(2, R.string.sans_serif) {
        override val fontFamily: FontFamily = FontFamily.SansSerif
    },
    SERIF(3, R.string.serif) {
        override val fontFamily: FontFamily = FontFamily.Serif
    },
    MONTSERRAT(4, R.string.font_montserrat) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.montserrat)) }
    },
    INTER(5, R.string.font_inter) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.inter)) }
    },
    OPEN_SANS(6, R.string.font_open_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.open_sans)) }
    },
    PLUS_JAKARTA_SANS(7, R.string.font_plus_jakarta_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.plus_jakarta_sans)) }
    },
    PLAYFAIR_DISPLAY(8, R.string.font_playfair_display) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.playfair_display)) }
    },
    OSWALD(9, R.string.font_oswald) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.oswald)) }
    },
    SOURCE_SANS_3(10, R.string.font_source_sans_3) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.source_sans_3)) }
    },
    RALEWAY(11, R.string.font_raleway) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.raleway)) }
    },
    NOTO_SANS(12, R.string.font_noto_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.noto_sans)) }
    },
    NUNITO(13, R.string.font_nunito) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.nunito)) }
    },
    WORK_SANS(14, R.string.font_work_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.work_sans)) }
    },
    RUBIK(15, R.string.font_rubik) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.rubik)) }
    },
    QUICKSAND(16, R.string.font_quicksand) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.quicksand)) }
    },
    HEEBO(17, R.string.font_heebo) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.heebo)) }
    },
    OUTFIT(18, R.string.font_outfit) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.outfit)) }
    },
    MANROPE(19, R.string.font_manrope) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.manrope)) }
    },
    CABIN(20, R.string.font_cabin) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.cabin)) }
    },
    COMFORTAA(21, R.string.font_comfortaa) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.comfortaa)) }
    },
    JOSEFIN_SANS(22, R.string.font_josefin_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.josefin_sans)) }
    },
    KARLA(23, R.string.font_karla) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.karla)) }
    },
    LORA(24, R.string.font_lora) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.lora)) }
    },
    LEXEND(25, R.string.font_lexend) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.lexend)) }
    },
    CRIMSON_PRO(26, R.string.font_crimson_pro) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.crimson_pro)) }
    },
    SOURCE_SERIF_4(27, R.string.font_source_serif_4) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.source_serif_4)) }
    },
    EB_GARAMOND(28, R.string.font_eb_garamond) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.eb_garamond)) }
    },
    EXO_2(29, R.string.font_exo_2) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.exo_2)) }
    },
    PUBLIC_SANS(30, R.string.font_public_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.public_sans)) }
    },
    ROBOTO_FLEX(31, R.string.font_roboto_flex) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.roboto_flex_var)) }
    },
    CAVEAT(32, R.string.font_caveat) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.caveat)) }
    },
    DANCING_SCRIPT(33, R.string.font_dancing_script) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.dancing_script)) }
    },
    PLAYPEN_SANS(34, R.string.font_playpen_sans) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.playpen_sans)) }
    },
    PACIFICO(35, R.string.font_pacifico) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.pacifico)) }
    },
    SHADOWS_INTO_LIGHT(36, R.string.font_shadows_into_light) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.shadows_into_light)) }
    },
    AMATIC_SC(37, R.string.font_amatic_sc) {
        override val fontFamily: FontFamily by lazy { FontFamily(Font(R.font.amatic_sc)) }
    }
    ;

    abstract val fontFamily: FontFamily

    companion object {
        fun fromId(id: Int): AppFont {
            return entries.find { it.id == id } ?: SYSTEM
        }
    }
}
