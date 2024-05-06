<?php
require_once('config.php');

$voter_id = $_POST['voter_id'];

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $sql = "UPDATE voters SET has_voted=1 WHERE voter_id=?";
    $stmt= $pdo->prepare($sql);
    $stmt->execute([$voter_id]);

    echo "Record updated successfully";
} catch (PDOException $e) {
    die($e->getMessage());
}
?>