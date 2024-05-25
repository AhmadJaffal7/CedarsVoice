<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Get the delay time and election ID from the POST parameters
    $delayTime = $_POST['delay_time'];
    $electionId = $_POST['electionId'];

    $query = "UPDATE elections SET delay_end = :delayTime WHERE election_id = :electionId";
    $stmt = $pdo->prepare($query);
    $stmt->bindParam(':delayTime', $delayTime, PDO::PARAM_INT);
    $stmt->bindParam(':electionId', $electionId, PDO::PARAM_INT);
    $stmt->execute();

    // Check if any rows were affected
    $rowCount = $stmt->rowCount();
    if ($rowCount > 0) {
        echo "Delay time saved successfully";
    } else {
        echo "Delay time not saved";
    }

    $stmt = null;
    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>