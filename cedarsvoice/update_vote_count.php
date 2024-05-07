<?php
// Include the database connection file
require_once('config.php');

// Get the candidate_id and vote_count from the POST data
$candidate_id = $_POST['candidate_id'];
$vote_count = $_POST['vote_count'];

try {
    // Establish database connection
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Prepare SQL statement to update the vote count
    $stmt = $pdo->prepare("UPDATE candidates SET vote_count = :vote_count WHERE candidate_id = :candidate_id");
    $stmt->bindParam(':vote_count', $vote_count, PDO::PARAM_STR);
    $stmt->bindParam(':candidate_id', $candidate_id);

    // Execute the query
    if ($stmt->execute()) {
        echo "Vote count updated successfully";
    } else {
        echo "Error updating vote count: " . $stmt->errorInfo()[2];
    }

    // Close the connection
    $pdo = null;
} catch (PDOException $e) {
    // Handle database connection error
    die($e->getMessage());
}
?>