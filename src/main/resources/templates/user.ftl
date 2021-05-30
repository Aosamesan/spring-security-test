<#import "./common.ftl" as common />

<@common.Outline title="Security Test | User Info">
    <div class="form-control mb-3">
        <#list CURRENT_USER.authorities as authority>
            <#switch authority>
                <#case "READ">
                    <span class="badge rounded-pill bg-primary">읽기</span>
                    <#break />
                <#case "WRITE">
                    <span class="badge rounded-pill bg-danger">쓰기</span>
                    <#break />
                <#case "WRITE_INFO">
                    <span class="badge rounded-pill bg-success">공지 쓰기</span>
                    <#break />
                <#case "USER_CONFIG">
                    <span class="badge rounded-pill bg-dark">회원 관리</span>
                    <#break />
            </#switch>
        </#list>
    </div>
    <form class="form-control" onsubmit="return false;">
        <div class="mb-3">
            <label for="username" class="form-label">아이디</label>
            <input type="text" id="username" class="form-control" name="username" value="${CURRENT_USER.username}" disabled/>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" id="password" class="form-control" name="password" required/>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">새 비밀번호 (설정시에만 입력)</label>
            <input type="password" id="new-password" class="form-control" name="newPassword" />
        </div>
        <div class="mb-3">
            <label for="confirm-password" class="form-label">새 비밀번호 확인</label>
            <input type="password" id="confirm-password" class="form-control" name="confirmPassword" />
        </div>
        <div class="mb-3">
            <label for="nickname" class="form-label">별명</label>
            <input type="text" id="nickname" class="form-control" name="nickname" value="${CURRENT_USER.nickname}" required/>
        </div>
        <div class="d-grid mb-3">
            <button id="submit-button" name="submitButton" class="btn btn-outline-success" type="button">수정하기</button>
        </div>
        <script>
            (function() {
                var password = document.getElementById("password");
                var confirmPassword = document.getElementById("confirm-password");
                var newPassword = document.getElementById("new-password");
                var submitButton = document.getElementById("submit-button");

                function checkPassword() {
                    if (newPassword.value && newPassword.value !== confirmPassword.value) {
                        alert("새 패스워드가 일치하지 않음");
                        return false;
                    } else if (!password.value) {
                        alert("패스워드가 비어있음");
                        return false;
                    }
                    return true;
                }

                submitButton.onclick = function() {
                    if (!checkPassword()) {
                        return false;
                    }
                    var nickname = document.getElementById("nickname");
                    var params = new URLSearchParams();
                    if (nickname.value) {
                        params.append("nickname", nickname.value);
                    }
                    if (newPassword.value) {
                        params.append("newPassword", newPassword.value);
                    }
                    params.append("password", password.value);
                    axios.put('/api/users', params).then(
                        () => window.location.href = "/"
                    )
                };

            })();
        </script>
    </form>
</@common.Outline>