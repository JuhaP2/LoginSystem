<?php

// Database connection credentials
$servername = "localhost";
$username = "root"; // Muuta nämä tarpeen mukaan
$password = ""; // Muuta nämä tarpeen mukaan
$database = "xxx";

// Luo yhteys
$conn = new mysqli($servername, $username, $password, $database);

// Tarkista yhteys
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Funktio uuden käyttäjän lisäämiseksi
function add_user($conn, $username, $password, $usertype) {
    // Hashataan salasana
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Valmistele kysely
    $stmt = $conn->prepare("INSERT INTO users (username, password, usertype) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $username, $hashed_password, $usertype);

    // Suorita kysely
    if ($stmt->execute()) {
        echo "User added successfully";
    } else {
        echo "Error adding user: " . $stmt->error;
    }
    $stmt->close();
}

// Esimerkkikäyttö
$username = "testi";  // Haluttu käyttäjänimi
$password = "testi";  // Haluttu salasana
$usertype = "user";  // Voit vaihtaa 'admin' jos käyttäjän tyyppi on admin

add_user($conn, $username, $password, $usertype);

$conn->close();
?>