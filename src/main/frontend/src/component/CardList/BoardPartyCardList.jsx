import React, { useState, useEffect} from 'react';
import './CardList.css';
import RecipeCard from './RecipeCard';
import axios from 'axios';
import Pagination from '../../lib/Pagination.jsx';
import { Link } from 'react-router-dom';
import jwt_decode from "jwt-decode";

const BoardRecipeCardList = () => {
    const [cards, setCards] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalRecipeCount, setTotalRecipeCount] = useState(0);
    const cardsPerPage = 6;


    useEffect(() => {
        const newToken = localStorage.getItem('login-token');
        const userNum = newToken ? jwt_decode(newToken).userNum : null;
        // 사용자 토큰에서 userNum 추출, 없으면 null

        const apiUrl = userNum ? `http://localhost:9999/recipe/list?userNum=${userNum}` : 'http://localhost:9999/recipe/list';

        axios.get(apiUrl)
            .then(response => {
                setCards(response.data);
                setTotalRecipeCount(response.data.length);
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    }, []);

    const offset = currentPage * cardsPerPage;
    const currentCards = cards.slice(offset, offset + cardsPerPage);

    const handlePageChange = (selectedPage) => {
        setCurrentPage(selectedPage);
    };

    return (
        <div className='board-card-list container'>
            <div className='board-list-top'>
                <p className='list-total-count'>전체 {totalRecipeCount} 개 </p>
                <Link to="/recipeWrite" className='write-go'>글작성</Link>
            </div>
            <div className="card-list">
                {Array.isArray(currentCards) && currentCards.map((card, index) => (
                    <RecipeCard key={index} card={card} showTitle={true} showLikeBox={true}/>
                ))}
            </div>
            <Pagination pageCount={Math.ceil(cards.length / cardsPerPage)} onPageChange={(data) => handlePageChange(data.selected)} />
        </div>
    );
};

export default BoardRecipeCardList;