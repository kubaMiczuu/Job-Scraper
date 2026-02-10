import React from "react";

const ThemeButton = ({toggleTheme, themeClasses, theme}) => {
    return (
        <div className="absolute right-0 top-0 flex items-center gap-3">

            <span className={`text-sm font-medium transition ${themeClasses}`}>Dark Theme</span>

            <label className="relative inline-flex items-center cursor-pointer">

                <input type="checkbox" className="sr-only peer" onChange={toggleTheme} checked={theme === 'dark'}/>

                <div className="w-11 h-6 bg-gray-400 rounded-full peer peer-checked:bg-blue-600 peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-0.5 after:left-0.5 after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all"></div>

            </label>

        </div>
    )
}
export default ThemeButton;