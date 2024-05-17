<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $start_time = $_POST['start_time'];
    $end_time = $_POST['end_time'];

    // Insert start and end times into the database
    $query = "INSERT INTO elections (start_time, end_time) VALUES (?, ?)";
    $stmt = $pdo->prepare($query);
    $stmt->bindValue(1, $start_time, PDO::PARAM_STR);
    $stmt->bindValue(2, $end_time, PDO::PARAM_STR);
    $stmt->execute();

    // Get the ID of the newly inserted record
    $newId = $pdo->lastInsertId();

    echo $newId;

    $stmt = null;
    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>