<?php
    
    $con=mysqli_connect("localhost", "root", "password", "DemTalk");
    $response = array();

    if (mysqli_connect_errno($con)) {
        $response["success"] = 0;
        $response["message"] = "Connection error" . $mysqli_connect_error();
        echo json_encode($response);
    } else {
        executeQuery();
    }
    
    mysqli_close($con);

function executeQuery() {
    
    $username = $_POST["username"];
    $password = $_POST["password"];

    $statement = mysqli_prepare($con, "SELECT * FROM 'Users' WHERE Username = ? AND Password = ?");
    mysqli_stmt_bind_param($statement, "ss", $username, $password);
    
    if (!(mysqli_stmt_execute($statement))) {
        $response["success"] = 0;
        $response["message"] = "Query error";
        echo json_encode($response);
    } else {
        mysqli_stmt_store_result($statement);
        mysqli_stmt_bind_result($statement, $userID, $name, $age, $username, $password);

        $user = array();
        while(mysqli_stmt_fetch($statement)) {
        $user["success"] = 1;
        $user["message"] = "Details match existing user";       
        $user["username"] = $username;
        $user["password"] = $password;    
    }
        echo json_encode($user);
    }
    
    mysqli_stmt_close($statement);
}
?>