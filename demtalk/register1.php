<?php

    $con=mysqli_connect("localhost", "root", "password", "DemTalk");

    $username = $_POST["username"];
    $password = $_POST["password"];

    $statement = mysqli_prepare($con, "INSERT INTO Users (username, password) VALUES (?, ?) ");
    mysqli_stmt_bind_param($statement, "ss", $username, $password);
    mysqli_stmt_execute($statement);

    mysqli_stmt_close($statement);
    
    mysqli_close($con);

?>