<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $start_time = $_POST['start_time'];
    $end_time = $_POST['end_time'];
    $supervisor_id = $_POST['supervisor_id']; // Assuming you receive supervisor ID as a string

    // Insert start and end times along with supervisor_id into the database
    $query = "INSERT INTO elections (start_time, end_time, supervisor_id) VALUES (?, ?, ?)";
    $stmt = $pdo->prepare($query);
    $stmt->bindValue(1, $start_time, PDO::PARAM_STR);
    $stmt->bindValue(2, $end_time, PDO::PARAM_STR);
    $stmt->bindValue(3, $supervisor_id, PDO::PARAM_INT);
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
