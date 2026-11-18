package `in`.hridayan.ashell.core.presentation.theme.domain.model

import `in`.hridayan.ashell.core.presentation.theme.color.toColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetColorSchemesTest {

    @Test
    fun presetColorSchemes_ashellyouSpecial_hasExpectedValues() {
        val special = PresetColorSchemes.AShellYouSpecial
        assertEquals(PresetColorSchemes.PRESET_ASHELLYOU_SPECIAL_ID, special.id)
        assertEquals("aShellYou Special", special.name)
        assertFalse(special.isDarkTheme)
        assertEquals("#1A835C", special.primary)
        assertEquals("#237E90", special.secondary)
        assertEquals("#8E6E0F", special.tertiary)
        assertEquals("#C83B60", special.error)
        assertEquals(special.surface, special.background)
    }

    @Test
    fun isPreset_identifiesPresetIdsCorrectly() {
        assertTrue(PresetColorSchemes.isPreset(PresetColorSchemes.PRESET_ASHELLYOU_SPECIAL_ID))
        assertTrue(PresetColorSchemes.isPreset(-2))
        assertFalse(PresetColorSchemes.isPreset(0))
        assertFalse(PresetColorSchemes.isPreset(1))
        assertFalse(PresetColorSchemes.isPreset(42))
    }

    @Test
    fun getPresetById_returnsMatchingPreset() {
        val preset = PresetColorSchemes.getPresetById(PresetColorSchemes.PRESET_ASHELLYOU_SPECIAL_ID)
        assertNotNull(preset)
        assertEquals("aShellYou Special", preset?.name)

        assertNull(PresetColorSchemes.getPresetById(1))
        assertNull(PresetColorSchemes.getPresetById(0))
    }

    @Test
    fun toColorScheme_convertsSpecialToComposeColorScheme() {
        val colorScheme = PresetColorSchemes.AShellYouSpecial.toColorScheme()
        assertNotNull(colorScheme)
    }
}
