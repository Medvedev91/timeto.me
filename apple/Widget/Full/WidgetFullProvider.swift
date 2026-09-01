import WidgetKit
import shared

struct WidgetFullProvider: TimelineProvider {
    
    private let widgetCache = WidgetCache()
    
    init() {
        InitKmpIosKt.doInitKmpIos()
    }
    
    func placeholder(
        in context: Context,
    ) -> WidgetFullEntry {
        WidgetFullEntry(date: Date(), widgetUi: nil)
    }
    
    func getSnapshot(
        in context: Context,
        completion: @escaping (WidgetFullEntry) -> (),
    ) {
        Task {
            let entry: WidgetFullEntry = await buildEntry(context: context)
            completion(entry)
        }
    }
    
    func getTimeline(
        in context: Context,
        completion: @escaping (Timeline<Entry>) -> (),
    ) {
        Task {
            let entry: WidgetFullEntry = await buildEntry(context: context)
            completion(Timeline(entries: [entry], policy: .atEnd))
        }
    }
    
    ///
    
    private func buildEntry(
        context: Context,
    ) async -> WidgetFullEntry {
        do {
            try await InitKmpKt.initKmpDeferred.await()
            
            let lastWidgetUpdateIdLocal: String? = try await KvDb.KEY.iosWidgetUpdateId.selectOrNull()?.value
            
            // Работа виджета ограничена 30mb. Данный метод вызывается часто,
            // чтобы оставаться в этих 30mb, прибегаю к кешированию.
            let widgetUi: WidgetUi
            if let lastWidgetUi = widgetCache.lastWidgetUi,
               lastWidgetUpdateIdLocal == widgetCache.lastWidgetUpdateId {
                widgetUi = lastWidgetUi
            } else {
                let newWidgetUi = try await WidgetUi.companion.buildIos(
                    width: Float(context.displaySize.width - (widgetFullHPadding * 2)),
                    rowHeight: Float(widgetFullItemHeight),
                    spacing: 8,
                )
                widgetUi = newWidgetUi
                widgetCache.lastWidgetUi = newWidgetUi
                widgetCache.lastWidgetUpdateId = lastWidgetUpdateIdLocal
            }
            
            return WidgetFullEntry(date: Date(), widgetUi: widgetUi)
        } catch {
            reportApi("WidgetFullProvider.buildEntry() error: \(error)")
            return WidgetFullEntry(date: Date(), widgetUi: nil)
        }
    }
}

private class WidgetCache {
    var lastWidgetUi: WidgetUi?
    var lastWidgetUpdateId: String?
}
