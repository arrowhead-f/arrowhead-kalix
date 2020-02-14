package eu.arrowhead.kalix.http;

import java.util.List;
import java.util.Objects;

/**
 * Routes incoming HTTP requests to {@link HttpRoute}s.
 */
class HttpRouter {
    final String basePath;
    final HttpRoute[] routes;

    HttpRouter(String basePath, HttpRoute[] routes) {
        this.basePath = basePath;
        this.routes = routes;
    }

    private interface RouteNode {
        boolean tryRoute(List<String> segments, int index, HttpRouteTask task);
    }

    private static final class RouteSegment implements RouteNode {
        final String name;
        final boolean isParameter;
        final List<RouteNode> routes;

        RouteSegment(final String name, final List<RouteNode> routes) {
            this.name = name;
            this.isParameter = name.startsWith(":");
            this.routes = routes;
        }

        @Override
        public boolean tryRoute(final List<String> segments, int index, final HttpRouteTask task) {
            if (isParameter) {
                task.getQueryParameters().put(name, segments.get(index));
            }
            else if (!Objects.equals(name, segments.get((index)))) {
                return false;
            }
            index += 1;
            for (final var route : routes) {
                if (route.tryRoute(segments, index, task)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class RouteLeaf implements RouteNode {
        final String name;
        final boolean isParameter;
        final HttpRoute route;

        RouteLeaf(final String name, final HttpRoute route) {
            this.name = name;
            this.isParameter = name.startsWith(":");
            this.route = route;
        }

        @Override
        public boolean tryRoute(final List<String> segments, int index, final HttpRouteTask task) {
            if (isParameter) {
                task.getQueryParameters().put(name, segments.get(index));
            }
            else if (!Objects.equals(name, segments.get((index)))) {
                return false;
            }
            task.callback(route);
            return true;
        }
    }
}
