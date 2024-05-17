<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Get the new end time from the POST parameters
    $newEndTime = $_POST['end_time'];
    $electionId = $_POST['electionId'];

    $query = "UPDATE elections SET end_time = :newEndTime WHERE election_id = :electionId";
    $stmt = $pdo->prepare($query);
    
    $stmt->bindParam(':newEndTime', $newEndTime);
    $stmt->bindParam(':electionId', $electionId, PDO::PARAM_INT);
    
    $stmt->execute();

    // Check if any rows were affected
    $rowCount = $stmt->rowCount();
    if ($rowCount > 0) {
        echo "End time updated successfully";
    } else {
        echo "End time not updated";
    }

    $stmt = null;
    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>
