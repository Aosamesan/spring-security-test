<#import "./common.ftl" as common />

<#assign REPLY_COUNT_PER_PAGE = 5 />

<#macro printReplies isFrozen replies replyCount>
    <#local totalPage = ((replyCount / REPLY_COUNT_PER_PAGE)?ceiling) />
    <#if totalPage gt 0>
        <div id="reply-list" class="_reply" data-current-page="${totalPage}">
            <#list replies?chunk(REPLY_COUNT_PER_PAGE) as replies>
                <div class="_panel"<#if replies_has_next> style="display: none;"</#if> data-page="${replies_index + 1}">
                    <#list replies as reply>
                        <div class="card m-2 _reply_content">
                            <div class="card-body">
                                <h6 class="card-subtitle text-muted mb-2">
                                    ${reply.authorNickname}
                                    <#if !isFrozen && (CURRENT_USER.authorities?seq_contains("WRITE")) && (reply.authorUsername == CURRENT_USER.username)>
                                        <small>
                                            <a type="button" class="link-success _edit_reply" href="#" data-id="${reply.id}" data-bs-toggle="modal" data-bs-target="#edit-modal">수정</a>
                                        </small>
                                        <small>
                                            <a href="#" class="link-danger _delete_reply" data-id="${reply.id}">삭제</a>
                                        </small>
                                    </#if>
                                </h6>
                                <div class="text-end">
                                    <#if reply.updatedAt?? && reply.updatedAt?has_content>
                                        <span>${reply.updatedAt?string('yyyy년 M월 d일 H시 m분')}</span>
                                        <span class="text-decoration-line-through text-muted">${reply.createdAt?string('yyyy년 M월 d일 H시 m분')}</span>
                                    <#else>
                                        <span>${reply.createdAt?string('yyyy년 M월 d일 H시 m분')}</span>
                                    </#if>
                                </div>
                                <div class="card-text _reply_text">${reply.content?html?replace("[\r\n]", "<br />")}</div>
                            </div>
                        </div>
                    </#list>
                </div>
            </#list>
            <nav aria-label="replies page navigation">
                <ul class="pagination justify-content-center _navigation">
                    <li class="page-item _previous"><a class="page-link" href="#reply-list">&laquo;</a></li>
                    <#list 1..*totalPage as page>
                        <li class="page-item _page<#if !page_has_next> active</#if>" data-page-index="${page}">
                            <a href="#reply-list" class="page-link">${page}</a>
                        </li>
                    </#list>
                    <li class="page-item disabled _next"><a class="page-link" href="#reply-list">&raquo;</a></li>
                </ul>
            </nav>
        </div>
        <div class="modal fade" id="edit-modal" tabindex="-1" aria-labelledby="Edit Reply Modal" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLabel">댓글 수정</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <label for="reply-edit-textarea" class="col-form-label">댓글</label>
                            <textarea class="form-control noresize" name="replyEditText" rows="15" id="reply-edit-textarea"></textarea>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-warning" data-bs-dismiss="modal">취소</button>
                        <button type="button" class="btn btn-success _edit_reply_button">수정</button>
                    </div>
                </div>
            </div>
        </div>
        <script>
            (function () {
                require(['jquery'], function ($) {
                    var $base = $('._reply');
                    var $navigation = $base.find('._navigation');
                    var $previous = $navigation.find('._previous');
                    var $next = $navigation.find('._next');
                    var $editModal = $('#edit-modal');
                    var $modalContent = $editModal.find('[name=replyEditText]');

                    $editModal.on('show.bs.modal', function (e) {
                        var $trigger = $(e.relatedTarget);
                        var $content = $trigger.closest('._reply_content').find('._reply_text');
                        var id = $trigger.data('id');
                        $modalContent.data('id', id);
                        $modalContent.text($content.text());
                    });

                    $editModal.on('hide.bs.modal', function (e) {
                        $modalContent.text('');
                    });

                    $editModal.find('._edit_reply_button').on('click', function (e) {
                        $.ajax('/api/replies/' + $modalContent.data('id'), {
                            method: 'PUT',
                            data: 'content=' + $modalContent.val(),
                            success: function () {
                                bootstrap.Modal.getInstance($editModal[0]).hide();
                                window.location.reload();
                            }
                        });
                    });

                    function movePage(page) {
                        var $selected = $navigation.find('._page[data-page-index=' + page + ']');
                        var $notSelected = $navigation.find('._page[data-page-index!=' + page + ']');
                        $base.data('current-page', page);
                        $selected.addClass('active');
                        $notSelected.removeClass('active');
                        if (page === 1) {
                            $previous.addClass('disabled');
                        } else {
                            $previous.removeClass('disabled');
                        }
                        if (page === ${totalPage}) {
                            $next.addClass('disabled');
                        } else {
                            $next.removeClass('disabled');
                        }
                        $base.find('._panel[data-page!=' + page + ']').hide();
                        $base.find('._panel[data-page=' + page + ']').show();
                    }

                    $navigation.find('._page').on('click', function (e) {
                        var $el = $(e.currentTarget);
                        var selectedPage = $el.data('page-index');
                        movePage(selectedPage);
                    });

                    $previous.on('click', function (e) {
                        var currentPage = Number.parseInt($base.data('current-page'));
                        if (currentPage > 1) {
                            movePage(currentPage - 1);
                        }
                    });

                    $next.on('click', function (e) {
                        var currentPage = Number.parseInt($base.data('current-page'));
                        if (currentPage < ${totalPage}) {
                            movePage(currentPage + 1);
                        }
                    });

                    $base.find('._delete_reply').on('click', function (e) {
                        e.preventDefault();
                        var id = $(e.currentTarget).data('id');
                        $.ajax('/api/replies/' + id, {
                            method: 'DELETE',
                            success: function() {
                                window.location.reload();
                            }
                        });
                    });
                });
            })();
        </script>
    </#if>
    <#if !isFrozen && CURRENT_USER.authorities?seq_contains("WRITE")>
        <form class="form-control mt-4" onsubmit="return false;">
            <div class="row m-1">
                <div class="col-10 p-0">
                    <label for="reply-content" class="form-label">댓글</label>
                    <textarea name="content" id="reply-content" rows="5" class="form-control noresize"></textarea>
                </div>
                <div class="col-2 p-3 d-flex">
                    <button id="add-reply-button" class="btn btn-primary flex-fill">등록</button>
                </div>
            </div>
        </form>
        <script>
            (function () {
                var replyContentDOM = document.getElementById("reply-content");
                var addReplyButtonDOM = document.getElementById("add-reply-button");

                function validate() {
                    if (!replyContentDOM.value) {
                        alert("내용이 비어있습니다.");
                        return false;
                    }
                    return true;
                }

                addReplyButtonDOM.onclick = function () {
                    if (validate()) {
                        var formData = new URLSearchParams();
                        formData.append("content", replyContentDOM.value);
                        axios.post("/api/documents/${DOCUMENT.displayId}/replies", formData)
                            .then(() => window.location.reload());
                    }
                };
            })();
        </script>
    </#if>
</#macro>

<@common.Outline DOCUMENT.title>
    <div class="d-grid">
        <figure>
            <blockquote class="blockquote">
                <h3>${DOCUMENT.title?html}</h3>
            </blockquote>
            <figcaption class="blockquote-footer text-end">
                ${DOCUMENT.authorNickname?html} (${DOCUMENT.authorUsername?html?replace("(.{3})(.*)", "$1\\*\\*\\*", "r")})
            </figcaption>
        </figure>
        <hr />
        <p>
            ${DOCUMENT.content?html?replace("[\r\n]", "<br />", "r")}
        </p>
        <hr />
        <div class="row">
            <div class="col">
                <div class="input-group">
                    <#if CURRENT_USER.authorities?seq_contains("WRITE_INFO")>
                        <button class="btn btn-secondary _freeze" data-id="${DOCUMENT.displayId}">
                            <#if DOCUMENT.frozen>
                                동결 해제
                            <#else>
                                동결
                            </#if>
                        </button>
                    </#if>
                    <#if CURRENT_USER.username == DOCUMENT.authorUsername>
                        <a href="/board/${DOCUMENT.displayId}/edit" class="btn btn-primary">수정</a>
                        <button class="btn btn-danger _delete_confirm" data-bs-toggle="modal" data-bs-target="#delete-modal">삭제</button>
                    </#if>
                </div>
            </div>
            <div class="col">
                <figure class="text-end">
                    <blockquote class="blockquote">
                        <p>${DOCUMENT.createdAt?string('yyyy년 MM월 dd일 HH:mm:ss')}에 작성</p>
                    </blockquote>
                    <#if DOCUMENT.updatedAt?? && DOCUMENT.updatedAt?has_content && DOCUMENT.updatedAt?is_date>
                        <figcaption class="blockquote-footer">
                            ${DOCUMENT.updatedAt?string('yyyy년 MM월 dd일 HH:mm:ss')}에 최종 수정
                        </figcaption>
                    </#if>
                </figure>
            </div>
        </div>
        <hr />
        <@printReplies DOCUMENT.frozen DOCUMENT.replies DOCUMENT.replyCount />
    </div>
    <div class="modal fade" id="delete-modal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">삭제 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p>글을 삭제하면 글에 달린 댓글도 모두 삭제되며 <strong>한 번 삭제된 글은 복원할 수 없습니다.</strong></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-danger _delete_button" data-id="${DOCUMENT.displayId}">삭제</button>
                </div>
            </div>
        </div>
    </div>
    <script>
        (function() {
            require(['jquery'], function ($) {
                $('._freeze').on('click', function (e) {
                    var $el = $(e.currentTarget);
                    $.ajax('/api/documents/freeze/' + $el.data('id'), {
                        method: 'PUT',
                        success: function () {
                            window.location.reload();
                        }
                    })
                });

                $('._delete_button').on('click', function (e) {
                   $.ajax('/api/documents/' + $(e.currentTarget).data('id'), {
                       method: 'DELETE',
                       success: function () {
                           window.location.href = '/board';
                       }
                   });
                });
            });
        })();
    </script>
</@common.Outline>