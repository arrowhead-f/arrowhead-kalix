package eu.arrowhead.kalix.http;

/**
 * Associates a full URL path with a {@link HttpRoute}.
 */
class HttpPathRoute implements HttpRoute {
    private final String path;
    private final HttpRoute route;

    HttpPathRoute(final String path, final HttpRoute route) {
        this.path = path;
        this.route = route;
    }

    String getPath() {
        return path;
    }

    @Override
    public Object handle(HttpRequest request, HttpResponse response) throws Exception {
          return route.handle(request, response);
    }
}
