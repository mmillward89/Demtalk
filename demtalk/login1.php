<?php

    $con=mysqli_connect("localhost", "root", "password", "DemTalk");
    
    $username = $_POST["username"];
    $password = $_POST["password"];
    
    $statement = mysqli_prepare($con, "SELECT * FROM Users WHERE Username = ? AND Password = ?");
    mysqli_stmt_bind_param($statement, "ss", $username, $password);
    mysqli_stmt_execute($statement);

    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $userID, $username, $password);

    $user = array();

    while(mysqli_stmt_fetch($statement)) {
        $user["username"] = $username;
        $user["password"] = $password;
    }

    echo json_encode($user);

    mysqli_stmt_close($statement);

    mysqli_close($con);

?>