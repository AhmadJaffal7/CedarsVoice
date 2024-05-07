<?php
// Include the database connection file
require_once('config.php');

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Get candidate_id from request parameters
    $candidate_id = $_GET['candidate_id'];

    // Prepare SQL statement to get the vote count
    $stmt = $pdo->prepare("SELECT vote_count FROM candidates WHERE candidate_id = :candidate_id");
    $stmt->bindParam(':candidate_id', $candidate_id);

    // Execute the query
    if ($stmt->execute()) {
        // Fetch the result
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        echo $result['vote_count'];
    } else {
        echo "Error getting vote count: " . $stmt->errorInfo()[2];
    }

    // Close the connection
    $pdo = null;
} catch (PDOException $e) {
    // Handle database connection error
    die($e->getMessage());
}
?>