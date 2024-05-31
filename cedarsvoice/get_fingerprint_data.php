<?php
// Include the config file
require_once 'config.php';

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Get the voter ID from the request
    $supervisor_id = $_GET['supervisor_id'];

    // Prepare SQL statement
    $sql = "SELECT supervisor_fingerprint FROM supervisors WHERE supervisor_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$supervisor_id]);

    // Check if a row is returned
    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $fingerprint = $row["supervisor_fingerprint"];

        $response = $fingerprint;
        echo $response;
    } else {
        echo "Fingerprint not found";
    }
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}

// Close connection
$pdo = null;
?>