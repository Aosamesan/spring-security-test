<#import "./common.ftl" as common />

<@common.Outline title="Security Test | Sign In">
    <form class="form-control" action="/login" method="POST">
        <div class="mb-3">
            <label for="username" class="form-label">아이디</label>
            <input type="text" id="username" class="form-control" name="username" required/>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" id="password" class="form-control" name="password" required/>
        </div>
        <div class="d-grid mb-3">
            <button class="btn btn-outline-success">Sign in</button>
        </div>
    </form>
</@common.Outline>