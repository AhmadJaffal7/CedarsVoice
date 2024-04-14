<?php
require_once('config.php');

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Get supervisor ID and police ID from POST request
    $supervisorID = $_POST['supervisorID'];
    $policeID = $_POST['policeID'];

    // Prepare SQL statement to select supervisor with given ID and police ID
    $stmt = $pdo->prepare("SELECT * FROM Supervisors WHERE supervisor_id = :supervisorID AND police_id = :policeID");
    $stmt->bindParam(':supervisorID', $supervisorID);
    $stmt->bindParam(':policeID', $policeID);
    $stmt->execute();

    // Check if supervisor exists
    if ($stmt->rowCount() > 0) {
        // Supervisor exists, send success response
        echo "success";
    } else {
        // Supervisor not found, send error response
        echo "error";
    }

    // Close database connection
    $pdo = null;
} catch (PDOException $e) {
    // Handle database connection error
    die($e->getMessage());
}
