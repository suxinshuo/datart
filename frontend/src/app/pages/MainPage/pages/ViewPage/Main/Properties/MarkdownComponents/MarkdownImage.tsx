import { useState } from 'react';
import Modal from 'react-modal';
import styled from 'styled-components/macro';

interface MarkdownImageProps {
  src?: string;
  alt?: string;
  title?: string;
}

const ImageContainer = styled.div`
  display: flex;
  justify-content: center;
  margin: 12px 0;

  img {
    max-width: 100%;
    max-height: 400px;
    cursor: pointer;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: scale(1.02);
    }
  }
`;

const PreviewModal = styled(Modal)`
  position: fixed;
  top: 50%;
  left: 50%;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 90vw;
  max-height: 90vh;
  padding: 20px;
  background-color: rgba(0, 0, 0, 0.9);
  border: none;
  border-radius: 8px;
  outline: none;
  transform: translate(-50%, -50%);

  &:focus {
    outline: none;
  }

  img {
    max-width: 100%;
    max-height: 80vh;
    border-radius: 4px;
  }
`;

const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.7);
`;

const CloseButton = styled.button`
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 8px;
  font-size: 20px;
  line-height: 1;
  color: white;
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 4px;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.8);
  }
`;

export const MarkdownImage = ({ src, alt, title }: MarkdownImageProps) => {
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  if (!src) return null;

  const openPreview = () => setIsPreviewOpen(true);
  const closePreview = () => setIsPreviewOpen(false);

  return (
    <>
      <ImageContainer>
        <img
          src={src}
          alt={alt || ''}
          title={title || ''}
          onClick={openPreview}
        />
      </ImageContainer>
      <PreviewModal
        isOpen={isPreviewOpen}
        onRequestClose={closePreview}
        contentLabel="Image Preview"
        overlayComponent={ModalOverlay}
      >
        <CloseButton onClick={closePreview}>×</CloseButton>
        <img src={src} alt={alt || ''} title={title || ''} />
      </PreviewModal>
    </>
  );
};
