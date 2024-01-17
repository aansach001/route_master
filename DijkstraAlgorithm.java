import java.sql.*;
import java.util.*;

public class DijkstraAlgorithm {
    static class Node {
        private final String node;
        private final double distance;

        Node(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        String getNode() {
            return node;
        }

        double getDistance() {
            return distance;
        }
    }

    static class PathInfo {
        private double distance;
        private List<String> path;

        PathInfo(double distance, List<String> path) {
            this.distance = distance;
            this.path = path;
        }

        double getDistance() {
            return distance;
        }

        List<String> getPath() {
            return path;
        }
    }

    private static Map<String, Map<String, Double>> buildGraph(ResultSet resultSet) throws SQLException {
        Map<String, Map<String, Double>> graph = new HashMap<>();

        while (resultSet.next()) {
            String source = resultSet.getString("Source");
            String destination = resultSet.getString("Destination");
            double weight = resultSet.getDouble("Distance");

            graph.computeIfAbsent(source, k -> new HashMap<>()).put(destination, weight);
            graph.computeIfAbsent(destination, k -> new HashMap<>()).put(source, weight);
        }

        return graph;
    }

    private static Map<String, PathInfo> dijkstra(Map<String, Map<String, Double>> graph, String source) {
        Map<String, PathInfo> paths = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String node : graph.keySet()) {
            paths.put(node,
                    new PathInfo(node.equals(source) ? 0.0 : Double.MAX_VALUE, new ArrayList<>(List.of(source))));
        }

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
                Comparator.nullsLast(Comparator.comparingDouble(node -> paths.get(node.getNode()).getDistance())));

        priorityQueue.add(new Node(source, 0.0));

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            String current = currentNode.getNode();

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            for (Map.Entry<String, Double> neighborEntry : graph.get(current).entrySet()) {
                String neighbor = neighborEntry.getKey();
                double edgeWeight = neighborEntry.getValue();

                double newDistance = paths.get(current).getDistance() + edgeWeight;

                if (newDistance < paths.get(neighbor).getDistance()) {
                    List<String> newPath = new ArrayList<>(paths.get(current).getPath());
                    newPath.add(neighbor);

                    paths.put(neighbor, new PathInfo(newDistance, newPath));

                    priorityQueue.add(new Node(neighbor, newDistance));
                }
            }
        }

        return paths;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/campusdistance";
            String user = "root";
            String password = "Admin@123";
            Connection con = DriverManager.getConnection(url, user, password);

            String fetchDataQuery = "SELECT * FROM campusdistan";
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(fetchDataQuery);

            Map<String, Map<String, Double>> graph = buildGraph(resultSet);

            System.out.print("Enter the source node: ");
            String sourceNode = scanner.nextLine();
            System.out.print("Enter the destination node: ");
            String destinationNode = scanner.nextLine();

            Map<String, PathInfo> paths = dijkstra(graph, sourceNode);

            PathInfo pathInfo = paths.get(destinationNode);
            double distance = pathInfo.getDistance();
            List<String> path = pathInfo.getPath();

            System.out.println("Shortest distance from " + sourceNode + " to " + destinationNode + ": " + distance);
            System.out.println("Shortest path: " + String.join(" -> ", path));

            resultSet.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
