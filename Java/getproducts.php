<?php
// Database connection credentials
$servername = "localhost";
$username = "root";
$password = "";
$database = "xxx";

// Create connection
$conn = new mysqli($servername, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Query to fetch products
$sql = "SELECT id, name, price, image FROM products";
$result = $conn->query($sql);

// Fetch data and return as JSON
if ($result->num_rows > 0) {
    $products = array();
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    echo json_encode($products);
} else {
    echo "0 results";
}
$conn->close();
?>
