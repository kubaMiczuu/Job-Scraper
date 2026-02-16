import { createRoot } from 'react-dom/client'
import './index.css'
import Dashboard from './pages/Dashboard'
import {StrictMode} from "react";

const rootElement = document.getElementById('root');
const root = createRoot(rootElement);

root.render(
    <StrictMode>
        <Dashboard/>
    </StrictMode>
)
