import React from 'react';
import { ConfigProvider, Layout } from 'antd';
import OSFChargen from './components/OSFChargen';
import './styles.css';

const App: React.FC = () => (
  <div className="appCenter">
      <ConfigProvider
        theme={{
          components: {
            Layout: {
              colorBgBody: "#f0ddc2"
            }
          }
        }}>
        <Layout style={{ padding: "0 50px" }}>
          <OSFChargen />
        </Layout>
      </ConfigProvider>
  </div>

);

export default App;
