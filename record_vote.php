<?php
require_once('config.php');

$voter_id = $_POST['voter_id'];
$candidate_id = $_POST['candidate_id'];

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $sql = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";
    $stmt= $pdo->prepare($sql);
    $stmt->execute([$voter_id, $candidate_id]);

    echo "Vote recorded successfully";
} catch (PDOException $e) {
    die($e->getMessage());
}
?>
