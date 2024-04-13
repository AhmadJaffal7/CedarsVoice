<?php
require_once('config.php');

try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);

    $first_name = $pdo->quote($_POST['first_name']);
    $last_name = $pdo->quote($_POST['last_name']);
    $age = $pdo->quote($_POST['age']);
    $description = $pdo->quote($_POST['description']);

    // Insert candidate details into the database
    $query = "INSERT INTO candidates (candidate_name, candidate_last_name, candidate_age, candidate_description) 
              VALUES ($first_name, $last_name, $age, $description)";

    if ($pdo->exec($query)) {
        echo "success";
    } else {
        echo "fail";
    }

    $pdo = null;
} catch (PDOException $e) {
    die($e->getMessage());
}
