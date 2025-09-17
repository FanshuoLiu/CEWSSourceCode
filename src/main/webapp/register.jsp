<%--
  Created by IntelliJ IDEA.
  User: lfs
  Date: 2024/9/21
  Time: 17:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>注册</title>
  <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  <style>
    #validationResult{
      color: red;
    }
  </style>
</head>
<body>
<h2>用户注册</h2>
<form id="registrationForm" method="post">
  <input type="hidden" name="action" value="register" id="action"/>
  <label>用户名的长度在5到15个字符之间,不允许有数字以及下划线之外的特殊符号</label></br>
  用户名: <input type="text" name="username" id="username" required /><br/>
  密码: <input type="password" name="password" id="password" required/><br/>
    电子邮箱：<input type="text" name="email" id="email" required/><br/>
  <input type="button" value="注册" id="register"/>
  <span id="validationResult"></span>
</form>
<p><a href="login.jsp">返回登录</a></p>
<script>
  $(document).ready(function () {
    $("#username").blur(function () {
      const username = $("#username").val();
      const regex = /^(?![0-9])(?=.{5,15}$)[a-zA-Z0-9_]+$/;
      if (regex.test(username)) {
        $('#validationResult').text('用户名符合规则').removeClass('invalid').addClass('valid');
      } else {
        $('#validationResult').text('无效用户名').removeClass('valid').addClass('invalid');
        return;
      }
    });

    $("#password").blur(function () {
      const password = $("#password").val();
      if (password.length < 5 || password.length > 15) {
        $('#validationResult').text('密码长度必须在5-15位之间').removeClass('valid').addClass('invalid');
        return;
      } else {
        $('#validationResult').text('密码符合规则').removeClass('invalid').addClass('valid');
      }
    });

    $("#email").blur(function () {
      const email = $("#email").val();
      const regex = /^[\w.-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (regex.test(email)) {
        $('#validationResult').text('邮箱格式正确').removeClass('valid').addClass('invalid');
      } else {
        $('#validationResult').text('邮箱格式不正确').removeClass('invalid').addClass('valid');
        return;
      }
    });

    $("#register").on("click", function () {
      const username = $("#username").val();
      const password = $("#password").val();
      const email = $("#email").val();
      const action = $("#action").val();
      // 简单检查是否为空
      if (username == "" || password == "" || email == "") {
        alert("请输入完整信息");
        return;
      }

      // 进行最终一次验证
      const regex = /^(?![0-9])(?=.{5,15}$)[a-zA-Z0-9_]+$/;
      if (!regex.test(username)) {
        alert("无效用户名");
        return;
      }
      if (password.length < 5 || password.length > 15) {
        alert("密码长度必须在5-15位之间");
        return;
      }
      const emailRegex = /^[\w.-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailRegex.test(email)) {
        alert("邮箱格式不正确");
        return;
      }

      // 进行 AJAX 提交
      $.ajax({
        url: 'UserServlet',
        type: 'POST',
        dataType: 'json', // 期待的响应数据类型
        data: {
          username: username,
          password: password,
          email: email,
          action: action
        },
        success: function (data) {
          if (data.status == "success") {
            alert(data.message);
            window.location.href = 'login.jsp';
          } else if (data.status == "error") {
            alert("注册失败:" + data.message);
            password.clear();
          }
        },
        error: function (jqXHR, textStatus, errorThrown) {
          alert("请求失败: " + textStatus);
        }
      });
    });
  });
</script>
</body>
</html>
