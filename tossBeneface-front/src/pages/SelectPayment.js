import React from 'react';
import { Link } from 'react-router-dom';

// Example of how to import the images
const SelectPayment = () => {
    return (
        <div style={styles.container}>
            <h1 style={styles.title}>결제 방식 선택</h1>
            <div style={styles.buttonsContainer}>
                <Link to="/qrcode/scan" style={styles.button}>
                    <img 
                        src="https://img.icons8.com/ios/452/qr-code.png" 
                        alt="QR Code" 
                        style={styles.image} 
                    />
                    <span style={styles.buttonText}>QR코드</span>
                </Link>
                <Link to="/faceRecognition" style={styles.button}>
                    <img 
                        src="/face.png" 
                        alt="Face Recognition" 
                        style={styles.image} 
                    />
                    <span style={styles.buttonText}>얼굴 인식</span>
                </Link>
            </div>
        </div>
    );
};

const styles = {
    container: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh', // Full height of the viewport
        backgroundColor: '#f7f7f7',
        padding: '20px',
        boxSizing: 'border-box',
    },
    title: {
        fontSize: '32px',
        marginBottom: '40px',
        color: '#333',
    },
    buttonsContainer: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        gap: '30px',
        width: '100%', // Full width of the container
        maxWidth: '800px', // Limit max width to keep everything in proportion
    },
    button: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '25px',
        fontSize: '20px',
        backgroundColor: '#007BFF',
        color: '#fff',
        border: 'none',
        borderRadius: '10px',
        textDecoration: 'none',
        cursor: 'pointer',
        transition: 'background-color 0.3s ease',
        width: '45%',
        height: '250px', // Set fixed height for each button
        boxSizing: 'border-box',
    },
    image: {
        width: '80px',  // Increase image size
        height: '80px', // Increase image size
        marginBottom: '15px',
    },
    buttonText: {
        fontSize: '18px',  // Increase font size for text
    }
};

export default SelectPayment;
