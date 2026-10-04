# Everlasting Summer 1.6 - in-game Android mod installer.
# Compiled with the same Ren'Py generation as the target APK.

init python:
    import os

    def es_mod_installer_open():
        if not renpy.android:
            renpy.notify(u"Установка модов доступна только на Android.")
            return

        try:
            from jnius import autoclass
            PythonSDLActivity = autoclass("org.renpy.android.PythonSDLActivity")
            Intent = autoclass("android.content.Intent")
            Picker = autoclass("su.sovietgames.everlasting_summer.ModPickerActivity")

            activity = PythonSDLActivity.mActivity
            intent = Intent(activity, Picker)
            activity.startActivity(intent)
        except Exception as e:
            renpy.notify(u"Не удалось открыть установщик модов.")
            print("ES Mod Installer error: " + repr(e))

    # The original APK already has a 'mods' screen. We add one small button
    # through the global overlay instead of replacing that screen.
    if "es_mod_install_overlay" not in config.overlay_screens:
        config.overlay_screens.append("es_mod_install_overlay")

screen es_mod_install_overlay():
    # get_screen() is non-None only while the existing mods screen is active.
    if renpy.get_screen("mods") is not None:
        textbutton u"Установить мод":
            xalign 0.78
            yalign 0.92
            action Function(es_mod_installer_open)
            style "log_button"
            text_style "settings_text"
