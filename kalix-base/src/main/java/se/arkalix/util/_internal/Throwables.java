package se.arkalix.util._internal;

public final class Throwables {
    private Throwables() {}

    public static boolean isFatal(final Throwable throwable) {
        return throwable instanceof VirtualMachineError ||
            throwable instanceof ThreadDeath ||
            throwable instanceof InterruptedException ||
            throwable instanceof LinkageError;
    }

    @SuppressWarnings("unchecked")
    public static <X extends Throwable> void throwSilently(final Throwable throwable) throws X {
        throw (X) throwable;
    }

    public static void throwSilentlyIfFatal(final Throwable throwable) {
        if (isFatal(throwable)) {
            throwSilently(throwable);
        }
    }

    public static void throwSilentlyIfFatalOrNull(final Throwable throwable) {
        if (throwable == null) {
            throw new NullPointerException();
        }
        if (isFatal(throwable)) {
            throwSilently(throwable);
        }
    }
}
