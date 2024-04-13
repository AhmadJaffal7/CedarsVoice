<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $first_name = $pdo->quote($_POST['first_name']);
    $last_name = $pdo->quote($_POST['last_name']);
    $national_id = $pdo->quote($_POST['national_id']);
    $fingerprint_data = $pdo->quote($_POST['fingerprint_data']);

    // $fingerprint_data = base64_decode($fingerprint_data);

    // Insert voter details and fingerprint data into the database
    $query = "INSERT INTO voters (voter_id, voter_name, voter_last_name, voter_fingerprint) 
              VALUES ($national_id, $first_name, $last_name, $fingerprint_data)";

    if ($pdo->exec($query)) {
        echo "success";
    } else {
        echo "fail";
    }

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
