
    // 전역 변수와 DOM 요소 참조를 초기화 함수로 분리
    let user_id, board_id, alertModal;
    let likeBtn, heartCountSpan, reportForm, reportModal;
    let deleteBtn, editBtn, cmtBtn;

    // 초기화 함수 - 모든 전역 변수와 이벤트 리스너 설정
    function initializeApp() {
        // 타임리프에서 전달된 데이터 가져오기
        const pageData = document.getElementById('pageData');
        user_id = pageData.dataset.userId;
        board_id = pageData.dataset.boardId;

        // DOM 요소 참조
        likeBtn = document.querySelector('#likeBtn');
        heartCountSpan = document.getElementById("heartCount");
        reportForm = document.getElementById('reportForm');
        reportModal = document.getElementById('reportModal');
        alertModal = new bootstrap.Modal(document.getElementById('alert'));
        deleteBtn = document.querySelector('#deleteBtn');
        editBtn = document.querySelector('#editBtn');
        cmtBtn = document.querySelector('#cmtBtn');

        // 이벤트 리스너 설정
        setupEventListeners();
    }

    // 알림창 함수 - 전역 함수로 정의
    function showAlert(message, redirectUrl = null) {
        document.querySelector("#alert .modal-body").innerHTML = message;
        alertModal.show();

        // 이벤트 리스너 중복 방지
        const confirmBtn = document.getElementById('confirmBtn');
        const newConfirmBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

        newConfirmBtn.addEventListener('click', function () {
            alertModal.hide();
            if (redirectUrl) {
                location.href = redirectUrl;
            }
        });
    }

    // 댓글 삭제 함수
    async function deleteComment(element) {
        const commentId = element.getAttribute("data-comment-id");

        if (confirm("정말 삭제하시겠습니까?")) {
            try {
                const response = await fetch('./comment/'+commentId, {
                    method: 'delete'
                });

                if (response.ok) {
                    location.reload();
                } else {
                    showAlert("삭제 실패! 서버 응답: " + response.status);
                }
            } catch (error) {
                console.error('Error:', error);
                showAlert("삭제 중 오류가 발생했습니다.");
            }
        }
    }

    // 모든 이벤트 리스너 설정
    function setupEventListeners() {
        // 좋아요 버튼 이벤트
        if (likeBtn) {
            likeBtn.addEventListener('click', async function (e) {
                e.preventDefault();
                try {
                    const response = await fetch('./like', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            "user_id": user_id,
                            "board_id": board_id
                        })
                    });

                    if (response.ok) {
                        const data = await response.json();
                        heartCountSpan.textContent = data.heartCount;
                        likeBtn.classList.toggle('bi-heart-fill', !data.liked);
                        likeBtn.classList.toggle('bi-heart', data.liked);
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showAlert("좋아요 처리 중 오류가 발생했습니다.");
                }
            });
        }

        // 신고 폼 이벤트
        if (reportForm) {
            reportForm.addEventListener('submit', async (e) => {
                e.preventDefault();

                const reason = document.getElementById('reportReason').value;
                const modal = bootstrap.Modal.getInstance(reportModal);

                if (!reason) {
                    showAlert('신고 사유를 선택해주세요.');
                    return;
                }

                try {
                    const response = await fetch('./report', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            user_id: user_id,
                            board_id: board_id,
                            reason_id: reason
                        })
                    });

                    if (response.status === 409) {
                        showAlert("이미 신고한 게시물입니다.");
                        modal.hide();
                    } else if (response.ok) {
                        showAlert("신고가 완료되었습니다.");
                        modal.hide();
                    } else {
                        showAlert("오류가 발생했습니다. 다시 시도해주세요.");
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showAlert('신고 처리 중 오류가 발생했습니다.');
                }
            });
        }

        // 삭제 버튼 이벤트
        if (deleteBtn) {
            deleteBtn.addEventListener('click', async function (e) {
                e.preventDefault();

                if (!confirm("삭제하시겠습니까?")) {
                    return;
                }

                try {
                    const response = await fetch('./delete?board_id='+board_id, {
                        method: 'delete'
                    });

                    if (response.ok) {
                        showAlert("삭제되었습니다.", "./list");
                    } else {
                        showAlert("삭제 실패! 서버 응답: " + response.status);
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showAlert("삭제 중 오류가 발생했습니다.");
                }
            });
        }

        // 수정 버튼 이벤트
        if (editBtn) {
            editBtn.addEventListener("click", function() {
                $.ajax({
                    type: "POST",
                    url: "./check-auth/" + board_id,
                    success: function(response) {
                        if (response.authorized) {
                            location.href = "./edit/" + board_id;
                        } else {
                            showAlert("수정 권한이 없습니다.");
                        }
                    },
                    error: function() {
                        showAlert("권한 확인 중 오류가 발생했습니다.");
                    }
                });
            });
        }

        // 댓글 작성 버튼 이벤트
        if (cmtBtn) {
            cmtBtn.addEventListener('click', async function (e) {
                e.preventDefault();

                const cmtContent = document.querySelector('#cmtContent').value;

                if (!cmtContent.trim()) {
                    showAlert("댓글 내용을 입력해주세요.");
                    return;
                }

                try {
                    const response = await fetch('./comment', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            writer_id: user_id,
                            board_id: board_id,
                            content: cmtContent
                        })
                    });

                    if (response.ok) {
                        location.reload();
                    } else {
                        showAlert('댓글 등록에 실패했습니다.');
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showAlert("댓글 작성 중 오류가 발생했습니다.");
                }
            });
        }

        // 댓글 삭제 아이콘 이벤트
        document.querySelectorAll(".delete-icon").forEach(button => {
            button.addEventListener("click", function (event) {
                event.preventDefault();
                deleteComment(this);
            });
        });
    }

    // DOM이 로드되면 앱 초기화
    document.addEventListener('DOMContentLoaded', initializeApp);