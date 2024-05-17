<?php

require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $first_name = $pdo->quote($_POST['first_name']);
    $last_name = $pdo->quote($_POST['last_name']);
    $national_id = $pdo->quote($_POST['national_id']);
    $fingerprint_data = $pdo->quote($_POST['fingerprint_data']);
    $fingerprint_iv = $pdo->quote($_POST['fingerprint_iv']);

    // Insert voter details, fingerprint data, and fingerprint IV into the database
    $query = "INSERT INTO voters (voter_id, voter_name, voter_last_name, voter_fingerprint, fingerprint_iv)
              VALUES ($national_id, $first_name, $last_name, $fingerprint_data, $fingerprint_iv)";

    if ($pdo->exec($query)) {
        echo "success";
    } else {
        echo "fail";
    }

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}