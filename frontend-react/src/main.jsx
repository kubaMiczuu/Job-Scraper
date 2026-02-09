import { createRoot } from 'react-dom/client'
import './index.css'
import Dashboard from './pages/Dashboard'

const rootElement = document.getElementById('root');
const root = createRoot(rootElement);

root.render(
    <Dashboard/>
)
