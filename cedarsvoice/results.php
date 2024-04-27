<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $getAllCandidates = "SELECT c.candidate_id, c.candidate_name, COUNT(v.vote_id) as votes 
                         FROM candidates c 
                         LEFT JOIN votes v ON c.candidate_id = v.candidate_id 
                         GROUP BY c.candidate_id, c.candidate_name";
    $result = $pdo->query($getAllCandidates);

    $return_array = array();

    while ($row = $result->fetch()) {
        $candidate = array();
        $candidate['candidate_id'] = $row['candidate_id'];
        $candidate['candidate_name'] = $row['candidate_name'];
        $candidate['votes'] = $row['votes'];
        $return_array[] = $candidate;
    }

    header('Content-Type: application/json');
    echo json_encode($return_array);

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
?>