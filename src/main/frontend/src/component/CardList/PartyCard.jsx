import React, { useState , useEffect} from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faHeart as faHeartSolid } from '@fortawesome/free-solid-svg-icons';
import { faHeart as faHeartRegular } from '@fortawesome/free-regular-svg-icons';
import { Link } from 'react-router-dom';
import jwt_decode from "jwt-decode";
import  axios from 'axios'
import Swal from "sweetalert2";
const PartyCard = ({ card, showTitle, showLikeBox }) => {
    const [userNum, setUserNum] = useState(0);
    const [postId, setPostId] = useState(card.postId);
    const [isLikedByUser, setIsLikedByUser] = useState(0);

    useEffect(() => {
        const newToken = localStorage.getItem('login-token');
        if (newToken) {
            const decodedToken = jwt_decode(newToken);
            if (decodedToken && decodedToken.userNum) {
                setUserNum(decodedToken.userNum);
            } else {
                console.error("userNum 없음");
            }
        } else {
            // 토큰이 없는 경우에 대한 처리
        }
    }, []);


    const checkLikeStatus = async () => {
        if (userNum) {
            try {
                const response = await axios.get(`http://localhost:9999/party/list?userNum=${userNum}`);
                const party = response.data.find(item => item.postId === card.postId);
                if (party) {
                    const liked = party.liked;
                    setIsLikedByUser(liked === 1);
                }
            } catch (error) {
                console.error('좋아요 상태 가져오기 실패:', error);
            }
        } else {
            setIsLikedByUser(false);
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            await checkLikeStatus(false);
            // 여기에서 페이지 전환을 수행하거나, 페이지 변경에 따른 로직을 추가할 수 있음
        };

        fetchData();
    }, [userNum, card.postId]);


    const handleToggleLike = () => {
        if (!userNum) {
            Swal.fire({
                icon: "warning",
                title: "알림",
                text: "로그인 후 좋아요 할 수 있어요!",
                showCancelButton: false,
                confirmButtonText: "확인"
            })
            setIsLikedByUser((prevIsLiked) => prevIsLiked);
        } else {
            setIsLikedByUser((prevIsLiked) => !prevIsLiked);
        axios.post(
            'http://localhost:9999/party/likes/toggle',
            { postId: postId, userNum: userNum },
            {
                headers: {
                    'Content-Type': 'application/json', // JSON 형식으로 보낼 것을 명시
                },
            }
        ).then((response) => {
                if (response.data === 'Like Inserted!') {
                    console.log('좋아요 추가됨');
                } else {
                    console.log('좋아요 취소됨');
                }
            })
            .catch((error) => {
                console.error('좋아요 토글 요청 실패:', error);
                // 좀 더 자세한 오류 정보를 클라이언트에 표시하려면 다음과 같이 수정합니다.
                if (error.response) {
                    console.error('서버 응답 상태 코드:', error.response.status);
                    // 500번오류일때 뜨게 하기alert("로그인을 하셨나요?")
                    console.error('서버 응답 데이터:', error.response.data);
                } else if (error.request) {
                    console.error('서버 응답 없음');
                } else {
                    console.error('요청 전 오류:', error.message);
                }
            });
        }
    };

    return (
        <div className="card">
            <Link to={`/partyDetail?postId=${card.postId}`}>
                <img className="card-img" src={`http://localhost:9999/party/image/${card.imageUrl}`} alt={card.title} />
            </Link>

            <div className='card-box'>
                {showTitle && (
                    <div className='card-title-box'>
                        <h2 className="cardList-title">{card.title}</h2>
                    </div>
                )}
                {showLikeBox && (
                    <div className='like-box'>
                        <button className='heart-btn' onClick={handleToggleLike}>
                            <FontAwesomeIcon icon={isLikedByUser ? faHeartSolid : faHeartRegular} style={{ color: isLikedByUser ? '#ff6a10' : 'black' }} />
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PartyCard;