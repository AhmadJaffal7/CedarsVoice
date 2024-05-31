<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    // Fetch all the end times from the database
    $query = "SELECT end_time FROM elections";
    $stmt = $pdo->prepare($query);
    $stmt->execute();
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $endTimes = array();
    foreach ($results as $row) {
        $endTimes[] = $row['end_time'];
    }

    echo implode(",", $endTimes);

    $stmt = null;
    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>