import React from 'react';
import { ConfigProvider, Layout } from 'antd';
import { GoogleOAuthProvider } from '@react-oauth/google';
import OSFChargen from './components/OSFChargen';
import './styles.css';

const App: React.FC = () => (
  <div className="appCenter">
      <GoogleOAuthProvider clientId='947921472325-r3tcbg3e5nfpesqpi5q91ltb7ds94m74.apps.googleusercontent.com'>
        <ConfigProvider
          theme={{
            components: {
              Layout: {
                bodyBg: "#f0ddc2"
              }
            }
          }}>
          <Layout style={{ padding: "0 50px" }}>
            <OSFChargen />
          </Layout>
        </ConfigProvider>
      </GoogleOAuthProvider>
  </div>

);

export default App;
