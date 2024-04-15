<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $getAllCandidates = "SELECT candidate_id, candidate_name FROM candidates";
    $result = $pdo->query($getAllCandidates);

    $return_array = array();

    while ($row = $result->fetch()) {
        $candidate = array();
        $candidate['candidate_id'] = $row['candidate_id'];
        $candidate['candidate_name'] = $row['candidate_name'];
        $return_array[] = $candidate;
    }

    header('Content-Type: application/json');
    echo json_encode($return_array);

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>
