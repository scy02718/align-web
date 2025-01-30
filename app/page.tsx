export default function Home() {
  return (
    <div className="w-full h-full flex items-center justify-center">
      <div className="flex flex-col items-center p-8 bg-white rounded-lg shadow-lg mt-10 hover:shadow-2xl transition duration-300">
        <h1 className="text-4xl font-bold">Align</h1>
        <p className="text-lg text-gray-500">A revoluationary tool for improving consultation efficiency</p>
        <p className="text-lg text-gray-500">and patient satisfaction</p>

        <div className="mt-10 flex gap-2 justify-between">
          <button className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition duration-300">Get Started</button>
          <button className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition duration-300">Learn More</button>
        </div>
      </div>
    </div>
  );
}
