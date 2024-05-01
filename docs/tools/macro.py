from typing import Optional

trace_icon      = ":fontawesome-solid-route:"
debug_icon      = ":fontawesome-solid-bug:"
info_icon       = ":fontawesome-solid-circle-info:"
warning_icon    = ":fontawesome-solid-triangle-exclamation:"
error_icon      = ":fontawesome-solid-circle-exclamation:"
stop_icon       = ":fontawesome-solid-circle-xmark:"
true_icon       = ":fontawesome-solid-square-check:"
false_icon      = ":fontawesome-solid-circle-times:"

windows_icon    = ":fontawesome-brands-windows:"
macos_icon      = ":fontawesome-brands-apple:"
linux_icon      = ":fontawesome-brands-linux:"

def define_env(env):
    define_macros(env.variables)

def define_macros(vars):
    vars['trace']       = f"{trace_icon}{{.trace}}"
    vars['debug']       = f"{debug_icon}{{.debug}}"
    vars['info']        = f"{info_icon}{{.info}}"
    vars['warning']     = f"{warning_icon}{{.warning}}"
    vars['error']       = f"{error_icon}{{.error}}"
    vars['stop']        = f"{stop_icon}{{.stop}}"

    # NOTE: `true` and `false` are reserved keywords
    vars['yes']         = f"{true_icon}{{.true}}"
    vars['no']          = f"{false_icon}{{.false}}"

    vars.os = dict(
        windows         = f"{windows_icon} Windows",
        linux           = f"{linux_icon} Linux",
        macos           = f"{macos_icon} macOS"
    )
