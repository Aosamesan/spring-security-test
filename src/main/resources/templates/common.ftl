<#macro Outline currentViewPath=VIEW_PATH!, currentUser=CURRENT_USER!, title="Security Test">
    <!DOCTYPE html>
    <html lang="ko">
        <head>
            <title>${title}</title>
            <meta charset="UTF-8"/>
            <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
            <link href="/static/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
            <script src="/static/js/bootstrap.min.js"></script>
        </head>
        <body>
            <@printNavigationBar currentViewPath currentUser! />
            <div class="container-fluid p-4">
                <#nested />
            </div>
        </body>
    </html>
</#macro>

<#macro printNavigationItem currentViewPath viewPath name>
    <#local isActive = (currentViewPath == viewPath) />
    <#if isActive>
        <a href="${viewPath}" class="nav-link active" aria-current="page">${name}</a>
    <#else>
        <a href="${viewPath}" class="nav-link">${name}</a>
    </#if>
</#macro>

<#macro printNavigationBar currentViewPath currentUser>
    <#local isLoggedOn = currentUser?has_content />
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a href="/" class="navbar-brand">Security Test</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbar-content"
                    aria-expanded="false" aria-label="Navbar Toggler">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbar-content">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                    <#if isLoggedOn>
                        <#if currentUser.authorities?seq_contains("READ")>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/board" "Board" />
                            </li>
                        </#if>
                        <#if currentUser.authorities?seq_contains("USER_CONFIG")>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/admin" "Admin" />
                            </li>
                        </#if>
                    <#else>
                        <li class="nav-item">
                            <@printNavigationItem currentViewPath "/login" "Sign In" />
                        </li>
                        <li class="nav-item">
                            <@printNavigationItem currentViewPath "/signup" "Sign Up" />
                        </li>
                    </#if>
                </ul>
                <#if isLoggedOn>
                    <form action="/logout" method="POST" class="d-flex">
                        <div class="input-group">
                            <a href="/user" class="btn <#if currentViewPath == "/user">btn-success<#else>btn-outline-success</#if>" type="button">${currentUser.nickname}</a>
                            <button class="btn btn-outline-danger" type="submit">Sign Out</button>
                        </div>
                    </form>
                </#if>
            </div>
        </div>
    </nav>
</#macro>