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

    public static <X extends Throwable> void throwSilentlyIfFatal(final Throwable throwable) {
        if (isFatal(throwable)) {
            throwSilently(throwable);
        }
    }
}
