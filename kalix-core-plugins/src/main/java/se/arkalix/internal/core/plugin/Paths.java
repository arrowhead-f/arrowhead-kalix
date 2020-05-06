package se.arkalix.internal.core.plugin;

import se.arkalix.util.annotation.Internal;

@Internal
public class Paths {
    private Paths() {}

    public static String combine(final CharSequence... paths) {
        if (paths.length == 0) {
            return "/";
        }
        var path = paths[0];
        if (path.length() == 0) {
            throw new IllegalArgumentException("Leading path may not be empty");
        }
        var builder = new StringBuilder(path);
        for (var i = 1; i < paths.length; ++i) {
            path = paths[i];
            if (path.length() > 0 && path.charAt(0) == '/') {
                builder = new StringBuilder(path);
                continue;
            }
            final var lastPath = paths[i - 1];
            if (lastPath.length() > 0 && lastPath.charAt(lastPath.length() - 1) != '/') {
                builder.append('/');
            }
            builder.append(path);
        }
        return builder.toString();
    }
}
