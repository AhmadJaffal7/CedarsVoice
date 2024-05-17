<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Get the election ID from the query parameter
    $electionId = $_GET['electionId'];

    // Fetch the end time from the database based on the election ID
    $query = "SELECT end_time FROM elections WHERE election_id = :electionId";
    $stmt = $pdo->prepare($query);
    $stmt->bindParam(':electionId', $electionId, PDO::PARAM_INT);
    $stmt->execute();
    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($result) {
        $endTime = $result['end_time'];
        echo $endTime;
    } else {
        echo "End time not found";
    }

    $stmt = null;
    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>