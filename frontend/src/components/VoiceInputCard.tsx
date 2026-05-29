const bars = ['h-4', 'h-8', 'h-12', 'h-7', 'h-10', 'h-5', 'h-9', 'h-6']

function VoiceInputCard() {
  return (
    <section className="rounded-[28px] border border-white/10 bg-white/[0.06] p-5 shadow-2xl shadow-black/25 backdrop-blur-xl">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-white">语音输入</p>
          <p className="mt-1 text-xs text-slate-400">麦克风已就绪</p>
        </div>
        <span className="rounded-full border border-amber-200/20 bg-amber-200/10 px-3 py-1 text-xs font-medium text-amber-100">
          即将上线
        </span>
      </div>

      <div className="mt-5 flex h-28 items-center justify-center rounded-3xl border border-cyan-300/10 bg-cyan-300/[0.06]">
        <div className="flex items-center gap-2">
          {bars.map((height, index) => (
            <span
              className={`${height} w-2 rounded-full bg-gradient-to-t from-cyan-400 to-emerald-200 opacity-80`}
              key={`${height}-${index}`}
            />
          ))}
        </div>
      </div>

      <p className="mt-4 text-sm leading-6 text-slate-400">
        这里仅展示语音录入状态，不会访问浏览器麦克风权限。
      </p>
    </section>
  )
}

export default VoiceInputCard
