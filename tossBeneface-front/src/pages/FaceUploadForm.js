import React from 'react';
import Webcam from 'react-webcam';
import useFaceUpload from '../hooks/useFaceUpload';
import CircularProgress from '../components/CircularProgress';

const videoConstraints = { width: 1280, height: 720, facingMode: 'user' };

export default function FaceUploadForm() {
  const { angleIndex, subPhase, modelLoaded, progressValue, webcamRef, angles } = useFaceUpload();

  if (!modelLoaded) {
    return <div style={{ textAlign: 'center', padding: 30 }}>모델 로딩 중...</div>;
  }

  if (angleIndex >= angles.length || subPhase === 'done') {
    return (
      <div style={{ textAlign: 'center', padding: 30 }}>
        <h1>얼굴 등록이 모두 완료되었습니다!</h1>
      </div>
    );
  }

  return (
    <div style={{ position: 'relative', width: '100vw', height: '100vh', overflow: 'hidden' }}>
      <Webcam
        audio={false}
        ref={webcamRef}
        screenshotFormat="image/jpeg"
        videoConstraints={videoConstraints}
        style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'cover' }}
      />

      {subPhase === 'intro' && (
        <div style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', color: '#fff' }}>
          <h1 style={{ marginBottom: 20 }}>잠시 후에 얼굴 등록이 진행됩니다</h1>
          <CircularProgress value={progressValue} />
        </div>
      )}

      {(subPhase === 'detect' || subPhase === 'progress') && (
        <>
          <div style={{ position: 'absolute', top: 20, left: '50%', transform: 'translateX(-50%)', color: '#fff', backgroundColor: 'rgba(0,0,0,0.5)', padding: '10px 20px', borderRadius: 8 }}>
            <h1 style={{ margin: 0 }}>얼굴 등록 ({angleIndex + 1} / {angles.length})</h1>
            <h2 style={{ margin: 0 }}>현재 각도: {angles[angleIndex]}</h2>
          </div>
          <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 300, height: 300, border: '4px solid #007bff', borderRadius: '50%', display: 'flex', justifyContent: 'center', alignItems: 'center', background: 'rgba(0, 0, 0, 0.2)' }}>
            {subPhase === 'progress'
              ? <CircularProgress value={progressValue} />
              : <p style={{ color: '#fff', fontSize: 16 }}>얼굴을 원 안에 맞춰주세요</p>
            }
          </div>
        </>
      )}
    </div>
  );
}
