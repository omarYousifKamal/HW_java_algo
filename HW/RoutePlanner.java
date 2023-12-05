import java.util.*;

class City {
    //پرۆگرامێكی جاڤا به‌ به‌كارهێنانی ئه‌گۆیزی داكسترا و (دیپس فێرست سێرچ) بۆ دۆزینه‌وه‌ی كورترین راوت له‌ نیوان دوو شاردا، پێكهاتووه‌ له‌ چه‌ند كلاسێك وه‌ك كلاسی شار، سه‌یاره‌، و رێگاو بان، ده‌توانین شاره‌كان زیاد بكه‌ین و ره‌شی بكه‌ینه‌وه‌ هه‌مان شت بۆ رێگاو بان و سه‌یاره‌كانیش، ئه‌م میكسه‌ی دوو ئه‌گۆریزم مه‌ودا حساب ده‌كات له‌گه‌ڵ كاتی گه‌شت و راوته‌كان، ئامانجی سه‌ره‌كی پێشاندانی كورترین راوته‌ له‌ خالی ئه‌ی بۆ بی.
    private String name;

    public City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Vehicle {
    private double fuelEfficiency; // in km per liter

    public Vehicle(double fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    public double getFuelEfficiency() {
        return fuelEfficiency;
    }
}

class Route {
    private List<City> cities;

    public Route() {
        this.cities = new ArrayList<>();
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public List<City> getCities() {
        return cities;
    }

    public int calculateTotalDistance(RoutePlanner routePlanner) {
        int totalDistance = 0;
        for (int i = 0; i < cities.size() - 1; i++) {
            City currentCity = cities.get(i);
            City nextCity = cities.get(i + 1);
            totalDistance += routePlanner.getDistance(currentCity, nextCity);
        }
        return totalDistance;
    }

    public int calculateEstimatedTravelTime(Vehicle vehicle, RoutePlanner routePlanner) {
        int totalDistance = calculateTotalDistance(routePlanner);
        double fuelEfficiency = vehicle.getFuelEfficiency();

        // Assuming an average speed of 60 km/h
        double estimatedTravelTime = totalDistance / 60.0;

        // Adding 15 minutes for every 100 km as a simple estimate
        estimatedTravelTime += (totalDistance / 100) * 0.25;

        // Adding 30 minutes for each city visited
        estimatedTravelTime += (cities.size() - 1) * 0.5;

        return (int) Math.ceil(estimatedTravelTime);
    }
}

public class RoutePlanner {
    private Map<City, Map<City, Integer>> roadNetwork;

    public RoutePlanner() {
        roadNetwork = new HashMap<>();
    }

    public void addRoad(City source, City destination, int distance) {
        roadNetwork.computeIfAbsent(source, k -> new HashMap<>()).put(destination, distance);
        roadNetwork.computeIfAbsent(destination, k -> new HashMap<>()).put(source, distance);
    }

    public void removeRoad(City source, City destination) {
        roadNetwork.computeIfPresent(source, (k, v) -> {
            v.remove(destination);
            return v.isEmpty() ? null : v;
        });
        roadNetwork.computeIfPresent(destination, (k, v) -> {
            v.remove(source);
            return v.isEmpty() ? null : v;
        });
    }

    public void addCity(City city) {
        roadNetwork.putIfAbsent(city, new HashMap<>());
    }

    public int getDistance(City source, City destination) {
        return roadNetwork.getOrDefault(source, Collections.emptyMap()).getOrDefault(destination, -1);
    }

    public List<City> findShortestRoute(City start, City end) {
        Map<City, Integer> distances = new HashMap<>();
        Map<City, City> previousNodes = new HashMap<>();
        PriorityQueue<City> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            City current = queue.poll();

            if (current.equals(end)) {
                List<City> path = new ArrayList<>();
                while (previousNodes.containsKey(current)) {
                    path.add(current);
                    current = previousNodes.get(current);
                }
                path.add(start);
                Collections.reverse(path);
                return path;
            }

            for (Map.Entry<City, Integer> neighborEntry : roadNetwork.getOrDefault(current, Collections.emptyMap()).entrySet()) {
                City neighbor = neighborEntry.getKey();
                int newDistance = distances.get(current) + neighborEntry.getValue();

                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    public List<List<City>> findAllRoutes(City start, City end) {
        List<List<City>> allRoutes = new ArrayList<>();
        findAllRoutesHelper(start, end, new HashSet<>(), new ArrayList<>(), allRoutes);
        return allRoutes;
    }

    private void findAllRoutesHelper(City current, City end, Set<City> visited, List<City> currentRoute, List<List<City>> allRoutes) {
        visited.add(current);
        currentRoute.add(current);

        if (current.equals(end)) {
            allRoutes.add(new ArrayList<>(currentRoute));
        } else {
            for (City neighbor : roadNetwork.getOrDefault(current, Collections.emptyMap()).keySet()) {
                if (!visited.contains(neighbor)) {
                    findAllRoutesHelper(neighbor, end, visited, currentRoute, allRoutes);
                }
            }
        }

        visited.remove(current);
        currentRoute.remove(current);
    }

    public void displayRoadNetwork() {
        System.out.println("Road Network:");
        for (Map.Entry<City, Map<City, Integer>> entry : roadNetwork.entrySet()) {
            City source = entry.getKey();
            System.out.print(source + ": ");
            for (Map.Entry<City, Integer> neighborEntry : entry.getValue().entrySet()) {
                City destination = neighborEntry.getKey();
                int distance = neighborEntry.getValue();
                System.out.print(destination + "(" + distance + "km) ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        RoutePlanner routePlanner = new RoutePlanner();

        // Creating city objects
        City erbil = new City("Erbil");
        City baghdad = new City("Baghdad");
        City mosul = new City("Mosul");

        // Adding cities to the road network
        routePlanner.addCity(erbil);
        routePlanner.addCity(baghdad);
        routePlanner.addCity(mosul);

        // Adding connections and distances between cities
        routePlanner.addRoad(erbil, baghdad, 400); // Replace 400 with the actual distance
        routePlanner.addRoad(baghdad, mosul, 300); // Replace 300 with the actual distance

        // Displaying the road network
        routePlanner.displayRoadNetwork();

        // Finding and printing the shortest route
        List<City> shortestRoute = routePlanner.findShortestRoute(erbil, mosul);
        System.out.println("Shortest Route: " + shortestRoute);

        // Finding and printing all possible routes
        List<List<City>> allRoutes = routePlanner.findAllRoutes(erbil, mosul);
        System.out.println("All Possible Routes: " + allRoutes);

        // Creating a vehicle
        Vehicle car = new Vehicle(15); // 15 km per liter fuel efficiency

        // Creating a route and calculating total distance
        Route customRoute = new Route();
        customRoute.addCity(erbil);
        customRoute.addCity(baghdad);
        customRoute.addCity(mosul);

        int totalDistance = customRoute.calculateTotalDistance(routePlanner);
        System.out.println("Total Distance of Custom Route: " + totalDistance + "km");

        // Calculating estimated travel time
        int estimatedTime = customRoute.calculateEstimatedTravelTime(car, routePlanner);
        System.out.println("Estimated Travel Time: " + estimatedTime + " hours");

        // Removing a road and updating the road network
        routePlanner.removeRoad(erbil, baghdad);
        System.out.println("Road between Erbil and Baghdad removed.");

       
        routePlanner.displayRoadNetwork();
    }
}
