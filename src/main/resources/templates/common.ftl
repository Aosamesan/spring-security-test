<#macro Outline title currentViewPath=VIEW_PATH!, currentUser=CURRENT_USER!>
    <#local title = "Security Test | ${title}" />
    <@compress single_line=true>
    <!DOCTYPE html>
    <html lang="ko">
        <head>
            <title>${title}</title>
            <meta charset="UTF-8"/>
            <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon" />
            <link href="/static/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
            <link rel="stylesheet" href="/static/css/common.css" type="text/css" />
            <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
            <script src="/static/js/bootstrap.min.js"></script>
            <script>
                var require = {
                    paths: {
                        'jquery': '/static/js/jquery-3.6.0.min'
                    }
                };
            </script>
            <script src="/static/js/require.js"></script>
        </head>
        <body>
            <@printNavigationBar currentViewPath currentUser! />
            <div class="container-fluid p-4">
                <#nested />
            </div>
        </body>
    </html>
    </@compress>
</#macro>

<#macro printNavigationItem currentViewPath viewPath name>
    <#local isActive = currentViewPath?starts_with(viewPath) />
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
            <a href="/" class="navbar-brand">게시판</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbar-content"
                    aria-expanded="false" aria-label="Navbar Toggler">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbar-content">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                    <#if isLoggedOn>
                        <#if currentUser.authorities?seq_contains("READ")>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/board" "게시판" />
                            </li>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/board/my" "내가 쓴 글" />
                            </li>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/replies/my" "내가 쓴 댓글" />
                            </li>
                        </#if>
                        <#if currentUser.authorities?seq_contains("USER_CONFIG")>
                            <li class="nav-item">
                                <@printNavigationItem currentViewPath "/admin" "회원 관리" />
                            </li>
                        </#if>
                    <#else>
                        <li class="nav-item">
                            <@printNavigationItem currentViewPath "/login" "로그인" />
                        </li>
                        <li class="nav-item">
                            <@printNavigationItem currentViewPath "/signup" "가입" />
                        </li>
                    </#if>
                </ul>
                <#if isLoggedOn>
                    <form action="/logout" method="POST" class="d-flex">
                        <div class="input-group">
                            <a href="/user" class="btn <#if currentViewPath == "/user">btn-success<#else>btn-outline-success</#if>" type="button">${currentUser.nickname}</a>
                            <button class="btn btn-outline-danger" type="submit">로그아웃</button>
                        </div>
                    </form>
                </#if>
            </div>
        </div>
    </nav>
</#macro>

<#macro printPageNavigation pagingInfo total>
    <#local start = pagingInfo.start />
    <#local display = pagingInfo.display />
    <#local currentPage = pagingInfo.currentPage />
    <#local totalPage = (total / display)?floor + 1/>
    <#local isFirst = (currentPage == 1) />
    <#local isLast = (currentPage == totalPage) />
    <#local pageStart = (currentPage > 4)?then(currentPage - 3, 1) />
    <#local pageEnd = (currentPage + 3 > totalPage)?then(totalPage, currentPage + 3) />
    <nav>
        <ul class="pagination justify-content-center">
            <li class="page-item<#if isFirst> disabled</#if>">
                <a href="?start=${start - display}&display=${display}" class="page-link" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <#list (pageStart..pageEnd) as idx>
                <li class="page-item<#if idx == currentPage> active</#if>">
                    <a href="?start=${(idx - 1) * display}&display=${display}" class="page-link">${idx}</a>
                </li>
            </#list>
            <li class="page-item<#if isLast> disabled</#if>">
                <a href="?start=${start + display}&display=${display}" class="page-link">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
</#macro>